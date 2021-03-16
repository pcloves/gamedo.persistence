package org.gamedo.persistence.db;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.gamedo.persistence.annotations.ComponentMap;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@ToString
public class EntityDbData implements DbData {
    /**
     * the id
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
     * the updater for incrementally update.
     */
    @Transient
    private volatile Updater updater;

    public EntityDbData(final String id, final Map<String, ComponentDbData> componentDbDataMap) {
        this.id = id == null ? new ObjectId().toString() : id;
        this.componentDbDataMap = new ConcurrentHashMap<>(componentDbDataMap == null ? Collections.emptyMap() : componentDbDataMap);
        this.updater = new SynchronizedUpdater(getMongoDbFieldName());
        this.componentDbDataMap.forEach((s, dbData) -> dbData.setId(EntityDbData.this.id));
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentDbData> T addComponentDbData(final T dbData) {

        final String idThat = dbData.getId();
        if (idThat != null && !idThat.equals(this.id)) {
            throw new RuntimeException("the dbData has belong to another EntityDbData, this id:" + this.id + ", that id:" + idThat + ", dbData:" + dbData);
        }

        dbData.setId(this.id);

        return (T) this.componentDbDataMap.put(dbData.getClass().getSimpleName(), dbData);
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentDbData> T getComponentDbData(final Class<T> dbData) {
        try {
            return (T) this.componentDbDataMap.get(dbData.getSimpleName());
        } catch (Exception e) {
            return null;
        }
    }

    public <T extends ComponentDbData> boolean hasComponentDbData(final Class<T> dbData) {
        return this.componentDbDataMap.containsKey(dbData.getSimpleName());
    }

    @Override
    public void setId(String id) {
        //do nothing.
    }

    @Override
    public String getMongoDbFieldName() {
        return "";
    }

    @Override
    public Updater getUpdater() {
        return updater;
    }

    @Override
    public void setUpdater(Updater update) {
        this.updater = update;
    }

    /**
     * set all componentDbData dirty
     */
    public void setAllComponentDbDataDirty() {
        componentDbDataMap.forEach((key, componentDbData) -> this.updater.setDirty(key, componentDbData));
    }

    /**
     * set the dbData dirty
     * @param dbData the dbData to be set
     * @param <T> the ComponentDbData child class
     * @return false if there is not a ComponentDbData of type T
     */
    public <T extends ComponentDbData> boolean setComponentDbDataDirty(Class<T> dbData) {

        final String key = dbData.getSimpleName();
        final ComponentDbData componentDbData = componentDbDataMap.get(key);
        if (componentDbData == null) {
            return false;
        }

        this.updater.setDirty(key, componentDbData);
        return true;
    }
}



