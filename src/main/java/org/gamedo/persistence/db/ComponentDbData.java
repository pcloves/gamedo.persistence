package org.gamedo.persistence.db;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
@ToString
public abstract class ComponentDbData implements DbData {

    /**
     * the entity's id belongs to.
     */
    @Transient
    private volatile String id;
    /**
     * the update of this ComponentDbData
     */
    @Transient
    private volatile Updater updater;

    @Transient
    private final String mongoDbFieldName;

    public ComponentDbData() {
        //We use the class's simple name as the field name.
        final Class<? extends ComponentDbData> clazz = getClass();
        this.mongoDbFieldName = clazz.getSimpleName();
        this.updater = new SynchronizedUpdater(getMongoDbFieldName());
    }

    @Override
    public Updater getUpdater() {
        return updater;
    }

    @Override
    public void setUpdater(Updater update) {
        this.updater = update;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getMongoDbFieldName() {
        return mongoDbFieldName;
    }
}
