package org.gamedo.persistence.db;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.ClassTypeInformation;

/**
 * 增量更新器的默认实现
 */
public class Updater implements IUpdater
{
    @Setter
    private static volatile MongoConverter mongoConverter;

    @Getter
    private final String prefix;
    private boolean isDirty;
    @Delegate
    private final Update update;

    public Updater(final String prefix) {
        this.prefix = prefix;
        update = new Update();
    }

    @Override
    public void update(final String key, final Object value) {
        final Object mongoType = mongoConverter.convertToMongoType(value, ClassTypeInformation.OBJECT);

        update.set(prefix + key, mongoType);
        isDirty = true;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

}
