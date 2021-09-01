package org.gamedo.persistence.event;

import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.gamedo.persistence.annotations.ComponentMap;
import org.gamedo.persistence.db.ComponentDbData;
import org.gamedo.persistence.db.EntityDbData;
import org.gamedo.persistence.logging.Markers;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;

import java.util.Objects;

@Log4j2
public class EntityDbDataAfterLoadEventListener extends AbstractMongoEventListener<EntityDbData> {
    private final String componentsMapFieldName;

    public EntityDbDataAfterLoadEventListener(MongoConverter mongoConverter) {
        final MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(EntityDbData.class);
        final MongoPersistentProperty property = Objects.requireNonNull(entity).getPersistentProperty(ComponentMap.class);

        componentsMapFieldName = Objects.requireNonNull(property).getFieldName();
    }

    @Override
    public void onAfterLoad(AfterLoadEvent<EntityDbData> event) {
        super.onAfterLoad(event);

        final Document document = event.getSource();

        try {
            //the EntityDbData.componentsDbDataMap has been unwrapped to key-value style on writing,
            // there shouldn't be a field with the same name.
            if (document.containsKey(componentsMapFieldName)) {
                log.error(Markers.MongoDB,
                        "the document should not contains field:{}, document:{}",
                        componentsMapFieldName,
                        document);
                return;
            }

            final Document componentDataDbMap = new Document();
            final String clazzName = document.getString(DefaultMongoTypeMapper.DEFAULT_TYPE_KEY);
            final Class<?> clazz = Class.forName(clazzName);

            //check whether the document class is valid.
            if (!EntityDbData.class.isAssignableFrom(clazz)) {
                log.error(Markers.MongoDB, "invalid class:{}, document:{}", clazz, document);
                return;
            }

            //remove all the ComponentDbData and wrap them into the EntityDbData.componentsDbDataMap
            document.entrySet().removeIf(entry -> {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                final boolean isComponentDbData = isComponentDbData(value);
                if (isComponentDbData) {
                    componentDataDbMap.put(key, value);
                }

                return isComponentDbData;
            });

            document.put(componentsMapFieldName, componentDataDbMap);

            log.debug(Markers.MongoDB, "reading convert finish, document:{}", () -> document);

        } catch (Exception e) {
            log.error(Markers.MongoDB, "exception caught on reading document:" + document, e);
        }
    }

    /**
     * check whether the object is a instance of ComponentDbData
     *
     * @param object the object to be check.
     * @return true the object's is a sub class of ComponentDbData
     */
    private static boolean isComponentDbData(final Object object) {
        try {

            if (!(object instanceof Document)) {
                return false;
            }

            final Document document = (Document) object;
            final String clazzName = document.getString(DefaultMongoTypeMapper.DEFAULT_TYPE_KEY);
            final Class<?> clazz = Class.forName(clazzName);

            return ComponentDbData.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            log.error(Markers.MongoDB, "class not found, object:" + object, e);
            return false;
        }
    }
}
