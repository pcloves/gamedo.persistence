package org.gamedo.persistence.converter;

import org.gamedo.persistence.config.MongoConfiguration;
import org.gamedo.persistence.db.EntityDbData;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
@ReadingConverter
public class EntityDbDataReadingConverter extends AbstractEntityDbDataReadingConverter<EntityDbData> {

    public EntityDbDataReadingConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}
