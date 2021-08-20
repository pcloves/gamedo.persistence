package org.gamedo.persistence.db;

/**
 * the DbData interface means it:
 * <ul>
 * <li>has an unique id for query/update</li>
 * <li>has an Update for incrementally serialization</li>
 * </ul>
 */
public interface DbData {
    /**
     * @return get the id mapping the _id field in MongoDB
     */
    String getId();

    /**
     * @param id set the id
     */
    void setId(String id);

    /**
     * Return the DbData document field name in MongoDB, supposing we have a MongoDB document:
     * <pre>{@code
     * {
     *     "_id" : ObjectId("604f66b6f695830356a6fd56"),
     *     "_class" : "org.gamedo.persistence.db.EntityDbPlayer",
     *     "ComponentDbBag" : {
     *         "itemList" : [
     *
     *         ],
     *         "_class" : "org.gamedo.persistence.db.ComponentDbBag"
     *     },
     *     "ComponentDbStatistic" : {
     *         "name" : "test",
     *         "_class" : "org.gamedo.persistence.db.ComponentDbStatistic"
     *     }
     * }
     * }</pre>
     * If the embedded document ComponentDbBag implement this DbData interface, The String of 'ComponentDbBag' should be
     * the return value of getMongoDbFieldName. If the whole document implement the DbData interface, the getMongoDbField
     * name should return an empty String.
     * @return return the full field name in MongoDB
     */
    String getMongoDbFieldName();

    /**
     * return the Updater of this DbData own.
     * @return the Updater is using.
     */
    Updater getUpdater();

    /**
     * set a new updater of this DbData
     * @param updater the new Updater
     */
    void setUpdater(Updater updater);

    /**
     * mark the key is dirty, and set new value for key
     * @param key the key to be set.
     * @param value the new value.
     */
    default void setDirty(String key, Object value) {
        getUpdater().setDirty(key, value);
    }

    /**
     * is the DbData clean or dirty(meaning that the method {@linkplain Updater#setDirty(String, Object)} has been called).
     * @return true if there are one or more keys set once.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean isDirty() {
        return getUpdater().isDirty();
    }
}
