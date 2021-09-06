package org.gamedo.persistence.db;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.gamedo.persistence.annotations.EntityDbDataComponent;
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

    /**
     * the {@link EntityDbData}'s clazz belongs to.
     */
    @Transient
    private final Class<? extends EntityDbData> entityDbDataClazz;

    protected ComponentDbData() {
        //We use the class's simple name as the field name.
        final Class<? extends ComponentDbData> clazz = getClass();
        entityDbDataClazz = clazz.getAnnotation(EntityDbDataComponent.class).value();
        mongoDbFieldName = clazz.getSimpleName();
        updater = new SynchronizedUpdater(getMongoDbFieldName());
    }

    @Override
    public Updater getUpdater() {
        return updater;
    }

    @Override
    public void setUpdater(Updater update) {
        updater = update;
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
