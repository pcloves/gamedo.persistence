package org.gamedo.persistence.converter;

import org.gamedo.persistence.db.EntityDbPlayer;
import org.gamedo.persistence.config.MongoConfiguration;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class EntityDbPlayerReadingConverter extends AbstractEntityDbDataReadingConverter<EntityDbPlayer> {
    public EntityDbPlayerReadingConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}
