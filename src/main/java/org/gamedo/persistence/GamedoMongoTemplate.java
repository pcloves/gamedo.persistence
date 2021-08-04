package org.gamedo.persistence;

import com.mongodb.client.result.UpdateResult;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.gamedo.persistence.db.DbData;
import org.gamedo.persistence.db.SynchronizedUpdater;
import org.gamedo.persistence.db.Updater;
import org.gamedo.persistence.logging.Markers;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("unused")
@Component
@Slf4j
public class GamedoMongoTemplate implements MongoOperations {

    private static final Executor ASYNC_POOL = new CompletableFuture<Void>().defaultExecutor();

    @Delegate(types = MongoOperations.class)
    private final MongoTemplate mongoTemplate;

    public GamedoMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    private void init() {
        SynchronizedUpdater.setMongoConverter(mongoTemplate.getConverter());
    }

    /**
     * save a DbData in another thread asynchronously.
     * @param data the data to be saved.
     * @param <T> the DbData child class
     * @return a CompletableFuture contains the data itself.
     */
    public <T extends DbData> CompletableFuture<T> saveAsync(final T data) {
        return saveAsyncInner(data, ASYNC_POOL);
    }

    /**
     * save a DbData in another thread asynchronously.
     * @param data the data to be saved.
     * @param executor the operating executor
     * @param <T> the DbData child class
     * @return a CompletableFuture contains the data itself.
     */

    public <T extends DbData> CompletableFuture<T> saveAsync(final T data, final Executor executor) {
        return saveAsyncInner(data, executor);
    }

    private <T extends DbData> CompletableFuture<T> saveAsyncInner(final T data, Executor executor) {
        final String className = data.getClass().getName();
        final String id = data.getId();
        final int hashCode = data.hashCode();
        final String mongoDbFieldName = data.getMongoDbFieldName();

        final Document document = new Document();
        final MongoConverter converter = mongoTemplate.getConverter();
        //serialize to Document on the caller thread.
        converter.write(data, document);
        //deserialize back to DbData
        @SuppressWarnings("unchecked")
        final T dbData = (T) converter.read(data.getClass(), document);

        return CompletableFuture.supplyAsync(() -> {

            if (log.isDebugEnabled()) {
                log.debug(Markers.MongoDB,
                        "saveAsync start, class:{}, id:{}, mongoDbFieldName:{}, data:({}){}",
                        className,
                        id,
                        mongoDbFieldName,
                        hashCode,
                        dbData);
            }
            final T savedData = mongoTemplate.save(dbData);
            if (log.isDebugEnabled()) {
                log.debug(Markers.MongoDB, "saveAsync finish, id:{}, hashCode:{}", id, hashCode);
            }

            return savedData;
        }, executor);
    }

    /**
     * Updates the first DbData that is found in the collection asynchronously. <b>Note that:</b>if the data hasn't saved before
     * yet, No query will matched, and won't insert a new document in the MongoDB either. This method has the consistent
     * behavior with {@linkplain MongoTemplate#updateFirst(Query, UpdateDefinition, Class)}
     * @param data the data to be update.
     * @param <T> the DbData child class
     * @return a CompletableFuture contains the UpdateResult.
     */
    public <T extends DbData> CompletableFuture<UpdateResult> updateFirstAsync(final T data) {
        return updateFirstAsyncInner(data, ASYNC_POOL);
    }

    /**
     * Updates the first DbData that is found in the collection asynchronously. <b>Note that:</b>if the data hasn't saved before
     * yet, No query will matched, and won't insert a new document in the MongoDB either. This method has the consistent
     * behavior with {@linkplain MongoTemplate#updateFirst(Query, UpdateDefinition, Class)}
     * @param data the data to be update.
     * @param executor the operating executor
     * @param <T> the DbData child class
     * @return a CompletableFuture contains the UpdateResult.
     */
    public <T extends DbData> CompletableFuture<UpdateResult> updateFirstAsync(final T data, Executor executor) {
        return updateFirstAsyncInner(data, executor);
    }

    /**
     * Updates the first DbData that is found in the collection asynchronously. <b>Note that:</b>if the data hasn't saved before
     * yet, No query will matched, and won't insert a new document in the MongoDB either. This method has the consistent
     * behavior with {@linkplain MongoTemplate#updateFirst(Query, UpdateDefinition, Class)}
     * @param data the data to be update.
     * @param executor the operating executor
     * @param <T> the DbData child class
     * @return a CompletableFuture contains the UpdateResult.
     */
    private  <T extends DbData> CompletableFuture<UpdateResult> updateFirstAsyncInner(final T data, final Executor executor) {

        final String mongoDbFieldName = data.getMongoDbFieldName();
        final Updater updater = data.getUpdater();
        final int hashCode = updater.hashCode();
        final Class<? extends DbData> clazz = data.getClass();
        final String className = clazz.getName();
        final String id = data.getId();
        if (!data.isDirty()) {
            log.warn(Markers.MongoDB, "the updater is not dirty, class:{}, id:{}, hashCode:{}, mongoDbFieldName:{}, updater:{}",
                    className,
                    id,
                    hashCode,
                    mongoDbFieldName,
                    updater);
            return CompletableFuture.completedFuture(UpdateResult.acknowledged(0,
                    0L,
                    null));
        }

        final Query query = new Query(Criteria.where("_id").is(id));
        final SynchronizedUpdater updateNew = new SynchronizedUpdater(mongoDbFieldName);

        data.setUpdater(updateNew);

        return CompletableFuture.supplyAsync(() -> {
            synchronized (updater) {
                if (log.isDebugEnabled()) {
                    log.debug(Markers.MongoDB, "updateFirstAsync start, class:{}, id:{}, mongoDbFieldName:{} updater:{}",
                            className,
                            id,
                            mongoDbFieldName,
                            updater);
                }
                final UpdateResult updateResult = mongoTemplate.updateFirst(query, updater, clazz);
                if (log.isDebugEnabled()) {
                    log.debug(Markers.MongoDB,
                            "updateFirstAsync finish, id:{}, hashCode:{}, result:{}",
                            id,
                            hashCode,
                            updateResult);
                }
                return updateResult;
            }
        }, executor);
    }
}
