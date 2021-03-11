package org.gamedo.persistence.db;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.query.Update;

@Getter
@Setter
@ToString
public abstract class ComponentDbData implements DbData {
    /**
     * the entity id that belongs to.
     */
    @Transient
    private volatile String id;
    /**
     * the update of this ComponentDbData
     */
    @Transient
    private volatile Update update;

    public ComponentDbData()
    {
        //We use the class's simple name as the field name.
        this.update = new SynchronizedUpdate(getClass().getSimpleName() + ".");
    }

    @Override
    public void setId(ObjectId id) {
        this.id = id.toString();
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
