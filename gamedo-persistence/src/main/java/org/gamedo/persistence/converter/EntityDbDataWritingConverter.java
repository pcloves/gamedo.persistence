package org.gamedo.persistence.converter;

import org.gamedo.persistence.config.MongoConfiguration;
import org.gamedo.persistence.db.EntityDbData;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
@WritingConverter
public class EntityDbDataWritingConverter extends AbstractEntityDbDataWritingConverter<EntityDbData> {

    public EntityDbDataWritingConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}

