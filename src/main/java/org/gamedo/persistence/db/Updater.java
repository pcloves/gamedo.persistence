package org.gamedo.persistence.db;

import org.springframework.data.mongodb.core.query.UpdateDefinition;

/**
 * An incremental Updater
 */
public interface Updater extends UpdateDefinition {

    /**
     * mark the key is dirty, and set new value for key
     * @param key the key to be set.
     * @param value the new value.
     */
    void setDirty(String key, Object value);

    /**
     * is the Updater clean or dirty(meaning that the method {@linkplain Updater#setDirty(String, Object)} has been called).
     * @return true if there are one or more keys set once.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isDirty();
}
