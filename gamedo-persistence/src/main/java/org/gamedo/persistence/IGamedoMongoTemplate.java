package org.gamedo.persistence;

import com.mongodb.client.result.UpdateResult;
import org.gamedo.persistence.db.ComponentDbData;
import org.gamedo.persistence.db.DbData;
import org.gamedo.persistence.db.EntityDbData;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("unused")
public interface IGamedoMongoTemplate {

    /**
     * save a DbData in another thread asynchronously.
     *
     * @param data the data to be saved.
     * @param <T>  the DbData child class
     * @return a CompletableFuture contains the data itself.
     */
    <T extends DbData> CompletableFuture<T> saveDbDataAsync(T data);

    /**
     * save a DbData in another thread asynchronously.
     *
     * @param data     the data to be saved.
     * @param executor the operating executor
     * @param <T>      the DbData child class
     * @return a CompletableFuture contains the data itself.
     */
    <T extends DbData> CompletableFuture<T> saveDbDataAsync(T data, Executor executor);

    /**
     * Updates the first DbData that is found in the collection asynchronously. <b>Note that:</b>if the data hasn't saved before
     * yet, No query will match, and won't insert a new document in the MongoDB either. This method has the consistent
     * behavior with {@linkplain MongoTemplate#updateFirst(Query, UpdateDefinition, Class)}
     *
     * @param data the data to be updated.
     * @param <T>  the DbData child class
     * @return a CompletableFuture contains the UpdateResult.
     */
    <T extends DbData> CompletableFuture<UpdateResult> updateDbDataFirstAsync(T data);

    /**
     * Updates the first DbData that is found in the collection asynchronously. <b>Note that:</b>if the data hasn't saved before
     * yet, No query will match, and won't insert a new document in the MongoDB either. This method has the consistent
     * behavior with {@linkplain MongoTemplate#updateFirst(Query, UpdateDefinition, Class)}
     *
     * @param data     the data to be updated.
     * @param executor the operating executor
     * @param <T>      the DbData child class
     * @return a CompletableFuture contains the UpdateResult.
     */
    <T extends DbData> CompletableFuture<UpdateResult> updateDbDataFirstAsync(T data, Executor executor);

    /**
     * find a {@link ComponentDbData}
     * @param id the EntityDbData id
     * @param componentClazz the component class
     * @param <T> the entity clazz type
     * @param <V> the component clazz type
     * @return return the componentDbData or null
     */
    <T extends EntityDbData, V extends ComponentDbData> CompletableFuture<V> findComponentDbDataByIdAsync(String id, Class<V> componentClazz);

    /**
     * find a {@link ComponentDbData}
     * @param id the EntityDbData id
     * @param componentClazz the component class
     * @param executor the operating executor
     * @param <T> the entity clazz type
     * @param <V> the component clazz type
     * @return return the componentDbData or null
     */
    <T extends EntityDbData, V extends ComponentDbData> CompletableFuture<V> findComponentDbDataByIdAsync(String id, Class<V> componentClazz, Executor executor);
}
