package com.gamedo.persistence.db;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Update;

/**
 * the DbData interface:
 * <li>has an unique id for query/save</li>
 * <li>has an Update for incrementally serialization</li>
 */
public interface DbData
{
    default void setId(String id) {}

    default void setId(ObjectId id) {}

    String getId();

    default String getDocumentKeyPrefix() {return "";}

    Update getUpdate();

    void setUpdate(Update update);
}
