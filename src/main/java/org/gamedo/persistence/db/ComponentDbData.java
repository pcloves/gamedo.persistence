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
    private volatile Updater update;

    @Transient
    private final String mongoDbFieldName;

    public ComponentDbData() {
        //We use the class's simple name as the field name.
        final Class<? extends ComponentDbData> clazz = getClass();
        this.mongoDbFieldName = clazz.getSimpleName();
        this.update = new SynchronizedUpdater(getMongoDbFieldName());
    }

    @Override
    public Updater getUpdater() {
        return update;
    }

    @Override
    public void setUpdater(Updater update) {
        this.update = update;
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
