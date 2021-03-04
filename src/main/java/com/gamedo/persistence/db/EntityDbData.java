package com.gamedo.persistence.db;

import com.gamedo.persistence.annotations.ComponentMap;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@ToString
public class EntityDbData implements DbData
{
    /**
     * entity db data id
     */
    @Id
    public final String id;
    /**
     * simple class name --> ComponentDbData
     */
    @Getter(AccessLevel.NONE)
    @ComponentMap
    @Field("_componentDbDataMap")
    private final Map<String, ComponentDbData> componentDbDataMap;
    /**
     * update for incremental save.
     */
    @Transient
    private volatile Update update;

    public EntityDbData(final String id, final Map<String, ComponentDbData> componentDbDataMap) {
        this.id = id == null ? new ObjectId().toString() : id;
        this.componentDbDataMap = new ConcurrentHashMap<>(componentDbDataMap == null ? Collections.emptyMap() : componentDbDataMap);
        this.update = new SynchronizedUpdate("");
        this.componentDbDataMap.forEach((s, dbData) -> dbData.setId(EntityDbData.this.id));
    }

    @PersistenceConstructor
    public EntityDbData(final ObjectId id, final Map<String, ComponentDbData> componentDbDataMap) {
        this(id.toString(), componentDbDataMap);
    }

    public <T extends ComponentDbData> T addComponentDbData(final T dbData) {

        dbData.setId(id);
        final DbData data = this.componentDbDataMap.put(dbData.getClass().getSimpleName(), dbData);

        return dbData;
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentDbData> T getComponentDbData(final Class<T> dbData)
    {
        try {
            return (T) this.componentDbDataMap.get(dbData.getSimpleName());
        } catch (Exception e) {
            return null;
        }
    }
}



