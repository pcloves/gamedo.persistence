package org.gamedo.persistence;

import com.mongodb.client.result.UpdateResult;
import org.gamedo.persistence.db.ComponentDbData;
import org.gamedo.persistence.db.DbData;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@SuppressWarnings("unused")
public interface IGamedoMongoTemplate {

    String ID_FIELD_NAME = "_id";

    /**
     * 使用{@link ForkJoinPool#commonPool()}线程池，异步存储一个{@link DbData}
     *
     * @param data 要存储的数据
     * @param <T>  要存储的数据类型
     * @return 返回执行该存储的CompletableFuture
     */
    <T extends DbData> CompletableFuture<T> saveDbDataAsync(T data);

    /**
     * 使用指定的线程池异步存储一个{@link DbData}
     *
     * @param data     要存储的数据
     * @param executor 要执行存储操作的线程池
     * @param <T>      要存储的数据类型
     * @return 返回执行该操作的CompletableFuture
     */
    <T extends DbData> CompletableFuture<T> saveDbDataAsync(T data, Executor executor);

    /**
     * 使用{@link ForkJoinPool#commonPool()}线程池，异步更新id为{@link DbData#getId()}的文档，<b>注意：</b>执行本方法前，需要确保本
     * data已经被持久化到mongoDB中，否则update操作不会被执行，本方法和{@linkplain MongoTemplate#updateFirst(Query, UpdateDefinition, Class)}
     * 具有相同的语义
     *
     * @param data 要更新的数据
     * @param <T>  要更新的数据类型
     * @return 返回执行该操作的CompletableFuture
     */
    <T extends DbData> CompletableFuture<UpdateResult> updateDbDataFirstAsync(T data);

    /**
     * 使用{@link ForkJoinPool#commonPool()}线程池，异步更新id为{@link DbData#getId()}的文档，<b>注意：</b>执行本方法前，需要确保本
     * data已经被持久化到mongoDB中，否则update操作不会被执行，本方法和{@linkplain MongoTemplate#updateFirst(Query, UpdateDefinition, Class)}
     * 具有相同的语义
     *
     * @param data     要更新的数据
     * @param executor 要执行更新操作的线程池
     * @param <T>      要更新的数据类型
     * @return 返回执行该操作的CompletableFuture
     */
    <T extends DbData> CompletableFuture<UpdateResult> updateDbDataFirstAsync(T data, Executor executor);

    /**
     * 使用{@link ForkJoinPool#commonPool()}线程池，异步加载一个组件数据
     *
     * @param id             组件所属文档的Id
     * @param componentClazz 组件所属{@link Class}
     * @param <V>            组件类型
     * @return 返回执行该操作的CompletableFuture，如果数据未加载到，则内部值为null
     */
    <V extends ComponentDbData> CompletableFuture<V> findComponentDbDataByIdAsync(Object id, Class<V> componentClazz);

    /**
     * 使用{@link ForkJoinPool#commonPool()}线程池，异步加载一个组件数据
     *
     * @param id             组件所属文档的Id
     * @param componentClazz 组件所属{@link Class}
     * @param executor       要执行加载操作的线程池
     * @param <V>            组件类型
     * @return 返回执行该操作的CompletableFuture，如果数据未加载到，则内部值为null
     */
    <V extends ComponentDbData> CompletableFuture<V> findComponentDbDataByIdAsync(Object id, Class<V> componentClazz, Executor executor);
}
