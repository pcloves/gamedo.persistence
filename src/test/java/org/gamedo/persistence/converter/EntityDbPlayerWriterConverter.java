package org.gamedo.persistence.converter;

import org.gamedo.persistence.config.MongoConfiguration;
import org.gamedo.persistence.db.EntityDbPlayer;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class EntityDbPlayerWriterConverter extends AbstractEntityDbDataWritingConverter<EntityDbPlayer> {
    public EntityDbPlayerWriterConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}
