package org.gamedo.persistence.converter;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.gamedo.persistence.annotations.ComponentMap;
import org.gamedo.persistence.config.MongoConfiguration;
import org.gamedo.persistence.db.EntityDbData;
import org.gamedo.persistence.logging.Markers;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

import java.util.Objects;

@Slf4j
public abstract class AbstractEntityDbDataWritingConverter<T extends EntityDbData> implements Converter<T, Document> {

    private final MongoConverter mongoConverter;
    private final String componentsMapFieldName;

    protected AbstractEntityDbDataWritingConverter(final MongoConfiguration configuration) {
        mongoConverter = configuration.getMongoConverter();

        final MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(EntityDbData.class);
        final MongoPersistentProperty property = Objects.requireNonNull(entity).getPersistentProperty(ComponentMap.class);

        //get the persistent field name.
        componentsMapFieldName = Objects.requireNonNull(property).getFieldName();
    }

    @Override
    public Document convert(T source) {

        final Document document = new Document();
        try {
            mongoConverter.write(source, document);

            //unwrap the EntityDbData.componentsDbDataMap to key-value style.
            final Document componentsMap = (Document) document.remove(componentsMapFieldName);
            document.putAll(componentsMap);

            if (log.isDebugEnabled()) {
                log.debug(Markers.MongoDB, "writing convert finish, source:{}, target:{}", source, document);
            }
        } catch (Exception e) {
            log.error(Markers.MongoDB, "exception caught, EntityDbData:" + source, e);
        }

        return document;
    }
}

