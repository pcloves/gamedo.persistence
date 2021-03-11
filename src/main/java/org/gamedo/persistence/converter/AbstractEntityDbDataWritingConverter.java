package org.gamedo.persistence.converter;

import org.gamedo.persistence.annotations.ComponentMap;
import org.gamedo.persistence.config.MongoConfiguration;
import org.gamedo.persistence.db.EntityDbData;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@Slf4j
public abstract class AbstractEntityDbDataWritingConverter<T extends EntityDbData> implements Converter<T, Document> {

    private final MongoConverter mongoConverter;
    private final String componentsMapFieldName;

    public AbstractEntityDbDataWritingConverter(final MongoConfiguration configuration) {
        this.mongoConverter = configuration.getMongoConverter();

        final MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(EntityDbData.class);
        final MongoPersistentProperty property = entity.getPersistentProperty(ComponentMap.class);

        //get the persistent field name.
        this.componentsMapFieldName = property.getFieldName();
    }

    @Override
    public Document convert(T source) {

        final Document document = new Document();
        try {
            mongoConverter.write(source, document);

            //unwrap the EntityDbData.componentsDbDataMap to key-value style.
            final Document componentsMap = (Document) document.remove(componentsMapFieldName);
            componentsMap.forEach(document::put);

            if (log.isDebugEnabled()) {
                log.debug("writing convert finish, source:{}, target:{}", source, document);
            }
        } catch (Exception e) {
            log.error("exception caught, EntityDbData:" + source, e);
        }

        return document;
    }
}

