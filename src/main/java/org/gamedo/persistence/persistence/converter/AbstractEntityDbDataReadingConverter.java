package org.gamedo.persistence.persistence.converter;

import org.gamedo.persistence.persistence.annotations.ComponentMap;
import org.gamedo.persistence.persistence.config.MongoConfiguration;
import org.gamedo.persistence.persistence.db.ComponentDbData;
import org.gamedo.persistence.persistence.db.EntityDbData;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@Slf4j
public abstract class AbstractEntityDbDataReadingConverter<T extends EntityDbData> implements Converter<Document, T> {

    private final MongoConverter mongoConverter;
    private final String componentsMapFieldName;

    public AbstractEntityDbDataReadingConverter(final MongoConfiguration configuration) {
        this.mongoConverter = configuration.getMongoConverter();

        final MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(EntityDbData.class);
        final MongoPersistentProperty property = entity.getPersistentProperty(ComponentMap.class);

        this.componentsMapFieldName = property.getFieldName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T convert(Document source) {

        try {
            //the EntityDbData.componentsDbDataMap has been unwrapped to key-value style when writing,
            // there shouldn't be a field with the same name.
            if (source.containsKey(componentsMapFieldName)) {
                log.error("the document should not contains field:{}, source:{}", componentsMapFieldName, source);
                return null;
            }

            final Document componentDataDbMap = new Document();
            final String clazzName = source.getString(DefaultMongoTypeMapper.DEFAULT_TYPE_KEY);
            final Class<?> clazz = Class.forName(clazzName);

            //check whether the source class is valid.
            if (!EntityDbData.class.isAssignableFrom(clazz)) {
                log.error("invalid class:{}, source:{}", clazz, source);
                return null;
            }

            //remove all of the ComponentDbData and wrap them into the EntityDbData.componentsDbDataMap
            source.entrySet().removeIf(entry -> {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                final boolean isComponentDbData = isComponentDbData(value);
                if (isComponentDbData) {
                    componentDataDbMap.put(key, value);
                }

                return isComponentDbData;
            });

            source.put(componentsMapFieldName, componentDataDbMap);

            final Object entityDbData = mongoConverter.read(clazz, source);

            if (log.isDebugEnabled()) {
                log.debug("reading convert finish, source:{}, target:{}", source, entityDbData);
            }

            return (T) entityDbData;
        } catch (Exception e) {
            log.error("exception caught on reading document:" + source, e);

            return null;
        }
    }

    /**
     * check whether the object is a instance of ComponentDbData
     *
     * @param object the object to be check.
     * @return true the object's is a sub class of ComponentDbData
     */
    private boolean isComponentDbData(final Object object) {
        try {

            if (!(object instanceof Document)) {
                return false;
            }

            final Document document = (Document) object;
            final String clazzName = document.getString(DefaultMongoTypeMapper.DEFAULT_TYPE_KEY);
            final Class<?> clazz = Class.forName(clazzName);

            return ComponentDbData.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
