package com.gamedo.converter;

import com.gamedo.persistence.config.MongoConfiguration;
import com.gamedo.persistence.converter.AbstractEntityDbDataWritingConverter;
import com.gamedo.db.EntityDbPlayer;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class EntityDbPlayerWriterConverter extends AbstractEntityDbDataWritingConverter<EntityDbPlayer> {
    public EntityDbPlayerWriterConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}
