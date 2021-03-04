package com.gamedo.persistence.converter;

import com.gamedo.persistence.config.MongoConfiguration;
import com.gamedo.persistence.db.EntityDbData;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class EntityDbDataReadingConverter extends AbstractEntityDbDataReadingConverter<EntityDbData> {

    public EntityDbDataReadingConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}
