package com.gamedo.converter;

import com.gamedo.persistence.config.MongoConfiguration;
import com.gamedo.persistence.converter.AbstractEntityDbDataReadingConverter;
import com.gamedo.db.EntityDbPlayer;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class EntityDbPlayerReadingConverter extends AbstractEntityDbDataReadingConverter<EntityDbPlayer> {
    public EntityDbPlayerReadingConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}
