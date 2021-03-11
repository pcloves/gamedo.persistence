package org.gamedo.persistence.db;

import lombok.Setter;
import lombok.Synchronized;
import org.bson.Document;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SynchronizedUpdate extends Update
{
    private static final String MAP_HOLDER = "MAP_HOLDER";
    @Setter
    private static MongoConverter CONVERTER = null;
    private final String keyPrefix;
    private final Object lock;

    public SynchronizedUpdate(String keyPrefix) {
        super();
        this.keyPrefix = keyPrefix;
        this.lock = this;
    }

    private Object convert(final Object value)
    {
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
        }
        else {
            CONVERTER.write(value, document);
            return document;
        }
    }

    @Synchronized("lock")
    public Update set(final String key, final Object value) {
        return super.set(keyPrefix + key, convert(value));
    }

    @Synchronized("lock")
    public Update setOnInsert(final String key, final Object value) {
        return super.setOnInsert(keyPrefix + key, convert(value));
    }

    @Synchronized("lock")
    public Update unset(final String key) {
        return super.unset(keyPrefix + key);
    }

    @Synchronized("lock")
    public Update inc(final String key, final Number inc) {
        return super.inc(keyPrefix + key, inc);
    }

    @Synchronized("lock")
    public void inc(final String key) {
        super.inc(keyPrefix + key);
    }

    @Synchronized("lock")
    public Update push(final String key, final Object value) {
        return super.push(keyPrefix + key, convert(value));
    }

    @Synchronized("lock")
    public PushOperatorBuilder push(final String key) {
        return super.push(keyPrefix + key);
    }

    /** @deprecated */
    @Deprecated
    @Synchronized("lock")
    @SuppressWarnings("deprecation")
    public Update pushAll(final String key, final Object[] values) {
        return super.pushAll(keyPrefix + key, values);
    }

    @Synchronized("lock")
    public AddToSetBuilder addToSet(final String key) {
        return super.addToSet(keyPrefix + key);
    }

    @Synchronized("lock")
    public Update addToSet(final String key, final Object value) {
        return super.addToSet(keyPrefix + key, convert(value));
    }

    @Synchronized("lock")
    public Update pop(final String key, final Position pos) {
        return super.pop(keyPrefix + key, pos);
    }

    @Synchronized("lock")
    public Update pull(final String key, final Object value) {
        return super.pull(keyPrefix + key, convert(value));
    }

    @Synchronized("lock")
    public Update pullAll(final String key, final Object[] values) {
        return super.pullAll(keyPrefix + key, values);
    }

    @Synchronized("lock")
    public Update rename(final String oldName, final String newName) {
        return super.rename(keyPrefix + oldName, keyPrefix + newName);
    }

    @Synchronized("lock")
    public Update currentDate(final String key) {
        return super.currentDate(keyPrefix + key);
    }

    @Synchronized("lock")
    public Update currentTimestamp(final String key) {
        return super.currentTimestamp(keyPrefix + key);
    }

    @Synchronized("lock")
    public Update multiply(final String key, final Number multiplier) {
        return super.multiply(keyPrefix + key, multiplier);
    }

    @Synchronized("lock")
    public Update max(final String key, final Object value) {
        return super.max(keyPrefix + key, convert(value));
    }

    @Synchronized("lock")
    public Update min(final String key, final Object value) {
        return super.min(keyPrefix + key, convert(value));
    }

    @Synchronized("lock")
    public BitwiseOperatorBuilder bitwise(final String key) {
        return super.bitwise(keyPrefix + key);
    }

    @Synchronized("lock")
    public Update isolated() {
        return super.isolated();
    }

    @Synchronized("lock")
    public Update filterArray(final CriteriaDefinition criteria) {
        return super.filterArray(criteria);
    }

    @Synchronized("lock")
    public Update filterArray(final String identifier, final Object expression) {
        return super.filterArray(identifier, expression);
    }

    @Synchronized("lock")
    public Boolean isIsolated() {
        return super.isIsolated();
    }

    @Synchronized("lock")
    public Document getUpdateObject() {
        return super.getUpdateObject();
    }

    @Synchronized("lock")
    public List<ArrayFilter> getArrayFilters() {
        return super.getArrayFilters();
    }

    @Override
    @Synchronized("lock")
    protected void addMultiFieldOperation(String operator, String key, Object value) {
        super.addMultiFieldOperation(operator, key, convert(value));
    }

    @Synchronized("lock")
    public boolean modifies(final String key) {
        return super.modifies(keyPrefix + key);
    }

    @Synchronized("lock")
    public boolean hasArrayFilters() {
        return super.hasArrayFilters();
    }
}
