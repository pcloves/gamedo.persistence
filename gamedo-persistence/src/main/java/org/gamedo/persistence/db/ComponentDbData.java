package org.gamedo.persistence.db;

import lombok.Data;
import org.springframework.data.annotation.Transient;

/**
 * {@link ComponentDbData}代表了一个组件存储数据
 */
@Data
public abstract class ComponentDbData<I> implements DbData<I> {

    /**
     * 所属{@link EntityDbData}的id
     */
    private volatile I id;
    /**
     * the update of this ComponentDbData
     */
    @Transient
    private transient volatile IUpdater updater;

    protected ComponentDbData() {
        //We use the class's simple name as the field name.
        updater = new Updater(getClass().getSimpleName() + ".");
    }

    @Override
    public IUpdater getUpdater() {
        return updater;
    }

    @Override
    public void setUpdater(IUpdater update) {
        updater = update;
    }

    @Override
    public I getId() {
        return id;
    }

    @Override
    public void setId(I id) {
        this.id = id;
    }
}
