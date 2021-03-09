package org.gamedo.persistence.persistence.converter;

import org.gamedo.persistence.persistence.config.MongoConfiguration;
import org.gamedo.persistence.persistence.db.EntityDbData;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class EntityDbDataWritingConverter extends AbstractEntityDbDataWritingConverter<EntityDbData> {

    public EntityDbDataWritingConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}

