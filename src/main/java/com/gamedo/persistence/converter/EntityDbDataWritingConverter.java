package com.gamedo.persistence.converter;

import com.gamedo.persistence.config.MongoConfiguration;
import com.gamedo.persistence.db.EntityDbData;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class EntityDbDataWritingConverter extends AbstractEntityDbDataWritingConverter<EntityDbData> {

    public EntityDbDataWritingConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}

