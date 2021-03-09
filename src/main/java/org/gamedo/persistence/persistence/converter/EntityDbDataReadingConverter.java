package org.gamedo.persistence.persistence.converter;

import org.gamedo.persistence.persistence.config.MongoConfiguration;
import org.gamedo.persistence.persistence.db.EntityDbData;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class EntityDbDataReadingConverter extends AbstractEntityDbDataReadingConverter<EntityDbData> {

    public EntityDbDataReadingConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}
