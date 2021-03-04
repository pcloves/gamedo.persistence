package com.gamedo.persistence.core;

import com.gamedo.persistence.db.DbData;
import com.gamedo.persistence.db.SynchronizedUpdate;
import com.mongodb.client.result.UpdateResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@EnableAsync
public class DbDataMongoTemplate {

    final MongoTemplate mongoTemplate;
    final TaskExecutor taskExecutor;

    public DbDataMongoTemplate(MongoTemplate mongoTemplate, TaskExecutor taskExecutor) {
        this.mongoTemplate = mongoTemplate;
        this.taskExecutor = taskExecutor;
    }

    @PostConstruct
    private void init() {
        SynchronizedUpdate.setCONVERTER(mongoTemplate.getConverter());
    }

    public <T extends DbData> CompletableFuture<T> save(final T data) {
        final T save = mongoTemplate.save(data);
        return CompletableFuture.completedFuture(save);
    }

    @Async
    public <T extends DbData> CompletableFuture<T> saveAsync(final T data) {
        return CompletableFuture.supplyAsync(() -> mongoTemplate.save(data), taskExecutor);
    }

    @SneakyThrows
    public <T extends DbData> CompletableFuture<UpdateResult> upsert(final T data) {

        if (log.isDebugEnabled()) {
            log.debug("begin save, class:{}, documentKeyPrefix:{} update:{}", data.getClass().getName(), data.getDocumentKeyPrefix(), data.getUpdate());
        }

        final Update update = data.getUpdate();
        final Query query = new Query(Criteria.where("_id").is(data.getId()));
        final SynchronizedUpdate updateNew = new SynchronizedUpdate(data.getDocumentKeyPrefix());
        data.setUpdate(updateNew);

        final CompletableFuture<UpdateResult> future = CompletableFuture.completedFuture(this.mongoTemplate.upsert(query, update, data.getClass()));
        if (log.isDebugEnabled()) {
            log.debug("save finish, result:{}", future.get());
        }

        return future;
    }

    @Async
    public <T extends DbData> CompletableFuture<UpdateResult> upsertAsync(final T data) {

        final String keyPrefix = data.getDocumentKeyPrefix();
        final Update update = data.getUpdate();
        final Query query = new Query(Criteria.where("_id").is(data.getId()));
        final SynchronizedUpdate updateNew = new SynchronizedUpdate(keyPrefix);

        data.setUpdate(updateNew);

        return CompletableFuture.supplyAsync(() -> {
            synchronized (update) {
                if (log.isDebugEnabled()) {
                    log.debug("begin save async, class:{}, documentKeyPrefix:{} update:{}", data.getClass().getName(), keyPrefix, update);
                }
                return this.mongoTemplate.upsert(query, update, data.getClass());
            }
        }, taskExecutor);
    }
}
