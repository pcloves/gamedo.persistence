package org.gamedo.persistence.event;

import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.gamedo.persistence.annotations.ComponentMap;
import org.gamedo.persistence.db.EntityDbData;
import org.gamedo.persistence.logging.Markers;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

import java.util.Objects;

@Log4j2
public class EntityDbDataBeforeSaveEventListener extends AbstractMongoEventListener<EntityDbData> {
    private final String componentsMapFieldName;

    public EntityDbDataBeforeSaveEventListener(MongoConverter mongoConverter) {
        final MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(EntityDbData.class);
        final MongoPersistentProperty property = Objects.requireNonNull(entity).getPersistentProperty(ComponentMap.class);

        componentsMapFieldName = Objects.requireNonNull(property).getFieldName();
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<EntityDbData> event) {
        super.onBeforeSave(event);

        final EntityDbData source = event.getSource();
        final Document document = event.getDocument();
        final Document componentsMap = (Document) Objects.requireNonNull(document).remove(componentsMapFieldName);

        document.putAll(componentsMap);

        log.debug(Markers.MongoDB, "writing convert finish, source:{}, target:{}", () -> source, () -> document);
    }
}
