package org.gamedo.persistence.converter;

import org.gamedo.persistence.db.EntityDbPlayer;
import org.gamedo.persistence.persistence.config.MongoConfiguration;
import org.gamedo.persistence.persistence.converter.AbstractEntityDbDataWritingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class EntityDbPlayerWriterConverter extends AbstractEntityDbDataWritingConverter<EntityDbPlayer> {
    public EntityDbPlayerWriterConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}
