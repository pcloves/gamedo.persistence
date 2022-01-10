package org.gamedo.persistence;

import com.mongodb.client.result.UpdateResult;
import lombok.experimental.Delegate;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.gamedo.persistence.db.ComponentDbData;
import org.gamedo.persistence.db.DbData;
import org.gamedo.persistence.db.Updater;
import org.gamedo.persistence.db.IUpdater;
import org.gamedo.persistence.logging.Markers;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.IndexOperationsProvider;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@SuppressWarnings("unused")
@Log4j2
public class GamedoMongoTemplate implements MongoOperations, IndexOperationsProvider, IGamedoMongoTemplate {

    private static final Executor ASYNC_POOL = ForkJoinPool.commonPool();
    @Delegate(types = MongoTemplate.class)
    private final MongoTemplate mongoTemplate;

    public GamedoMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        Updater.setMongoConverter(getConverter());
    }

    @Override
    public <T extends DbData> CompletableFuture<T> saveDbDataAsync(final T data) {
        return saveAsyncInner(data, ASYNC_POOL);
    }

    @Override
    public <T extends DbData> CompletableFuture<T> saveDbDataAsync(final T data, final Executor executor) {
        return saveAsyncInner(data, executor);
    }

    private <T extends DbData> CompletableFuture<T> saveAsyncInner(final T data, Executor executor) {
        final String className = data.getClass().getName();
        final Object id = data.getId();
        final int hashCode = data.hashCode();

        final Document document = new Document();
        final MongoConverter converter = getConverter();
        //serialize to Document on the caller thread.
        converter.write(data, document);
        //deserialize back to DbData
        @SuppressWarnings("unchecked") final T dbData = (T) converter.read(data.getClass(), document);

        return CompletableFuture.supplyAsync(() -> {

            log.debug(Markers.MongoDB,
                    "saveAsync start, class:{}, id:{}, data:({}){}",
                    () -> className,
                    () -> id,
                    () -> hashCode,
                    () -> dbData);

            final T savedData = save(dbData);
            log.debug(Markers.MongoDB, "saveAsync finish, id:{}, hashCode:{}", () -> id, () -> hashCode);

            return savedData;
        }, executor);
    }

    @Override
    public <T extends DbData> CompletableFuture<UpdateResult> updateDbDataFirstAsync(final T data) {
        return updateFirstAsyncInner(data, ASYNC_POOL);
    }

    @Override
    public <T extends DbData> CompletableFuture<UpdateResult> updateDbDataFirstAsync(final T data, Executor executor) {
        return updateFirstAsyncInner(data, executor);
    }

    @Override
    public <V extends ComponentDbData> CompletableFuture<V> findComponentDbDataByIdAsync(Object id,
                                                                                         Class<V> componentClazz)
    {
        return findComponentDbDataByIdInner(id, componentClazz, ASYNC_POOL);
    }

    @Override
    public <V extends ComponentDbData> CompletableFuture<V> findComponentDbDataByIdAsync(Object id,
                                                                                         Class<V> componentClazz,
                                                                                         Executor executor) {
        return findComponentDbDataByIdInner(id, componentClazz, executor);
    }

    private <V extends ComponentDbData> CompletableFuture<V> findComponentDbDataByIdInner(Object id,
                                                                                          Class<V> componentClazz,
                                                                                          Executor executor) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        query.fields().include(componentClazz.getSimpleName()).include(DefaultMongoTypeMapper.DEFAULT_TYPE_KEY);

        return CompletableFuture.supplyAsync(() -> find(query, componentClazz)
                .stream()
                .findFirst()
                .orElse(null), executor);
    }

    private <T extends DbData> CompletableFuture<UpdateResult> updateFirstAsyncInner(final T data, final Executor executor) {

        final IUpdater updater = data.getUpdater();
        final int hashCode = updater.hashCode();
        final Class<? extends DbData> clazz = data.getClass();
        final String className = clazz.getName();
        final Object id = data.getId();
        if (!data.isDirty()) {
            log.warn(Markers.MongoDB, "the updater is not dirty, class:{}, id:{}, hashCode:{}, updater:{}",
                    className,
                    id,
                    hashCode,
                    updater);
            return CompletableFuture.completedFuture(UpdateResult.acknowledged(0,
                    0L,
                    null));
        }

        final Query query = new Query(Criteria.where("_id").is(id));
        final Updater updateNew = new Updater(updater.getPrefix());

        data.setUpdater(updateNew);

        return CompletableFuture.supplyAsync(() -> {
            log.debug(Markers.MongoDB, "updateFirstAsync start, class:{}, id:{}, updater:{}",
                    () -> className,
                    () -> id,
                    () -> updater);
            final UpdateResult updateResult = updateFirst(query, updater, clazz);
            log.debug(Markers.MongoDB,
                    "updateFirstAsync finish, id:{}, hashCode:{}, result:{}",
                    () -> id,
                    () -> hashCode,
                    () -> updateResult);
            return updateResult;
        }, executor);
    }
}
