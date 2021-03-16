package org.gamedo.persistence.db;

import lombok.Setter;
import lombok.Synchronized;
import lombok.ToString;
import org.bson.Document;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A Synchronized Updater implementation.
 */
@ToString(exclude = "lock")
public class SynchronizedUpdater implements Updater
{
    private static final String MAP_HOLDER = "MAP_HOLDER";
    @Setter
    private static MongoConverter CONVERTER = null;
    private final String keyPrefix;
    private boolean isDirty = false;
    private final Update update;
    @SuppressWarnings("unused")
    private final Object lock;

    public SynchronizedUpdater(final String keyPrefix) {
        super();
        this.keyPrefix = (keyPrefix != null && keyPrefix.length() > 0) ? keyPrefix + "." : "";
        this.update = new Update();
        this.lock = this;
    }

    private Object convert(final Object value) {
        if (SimpleTypeHolder.DEFAULT.isSimpleType(value.getClass())) {
            return value;
        }

        final Document document = new Document();
        if (value instanceof Collection || value.getClass().isArray()) {

            //Collection or Array cannot be written directly.(see issue:https://github.com/spring-projects/spring-data-mongodb/issues/3570)
            //We use a trick to bypass it.
            final Map<String, Object> map = Collections.singletonMap(MAP_HOLDER, value);

            CONVERTER.write(map, document);
            return document.get(MAP_HOLDER);
        } else {
            CONVERTER.write(value, document);
            return document;
        }
    }

    @Synchronized("lock")
    public void setDirty(final String key, final Object value) {
        update.set(keyPrefix + key, convert(value));
        isDirty = true;
    }

    @Override
    @Synchronized("lock")
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    @Synchronized("lock")
    public Boolean isIsolated() {
        return update.isIsolated();
    }

    @Override
    @Synchronized("lock")
    public Document getUpdateObject() {
        return update.getUpdateObject();
    }

    @Override
    @Synchronized("lock")
    public boolean modifies(String key) {
        return update.modifies(key);
    }

    @Override
    @Synchronized("lock")
    public void inc(String key) {
        update.inc(key);
    }

    @Override
    @Synchronized("lock")
    public List<ArrayFilter> getArrayFilters() {
        return update.getArrayFilters();
    }
}
