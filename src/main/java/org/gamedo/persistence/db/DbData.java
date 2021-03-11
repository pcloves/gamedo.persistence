package org.gamedo.persistence.db;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Update;

/**
 * the DbData interface means it:
 * <ul>
 * <li>has an unique id for query/save</li>
 * <li>has an Update for incrementally serialization</li>
 * </ul>
 */
public interface DbData {
    String getId();

    default void setId(String id) {
    }

    default void setId(ObjectId id) {
    }

    default String getDocumentKeyPrefix() {
        return "";
    }

    Update getUpdate();

    void setUpdate(Update update);
}
