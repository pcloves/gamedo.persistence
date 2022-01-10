package org.gamedo.persistence.db;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document("player")
public class EntityDbPlayer extends EntityDbData {
    public EntityDbPlayer(Object id, Map<String, ComponentDbData> componentDbDataMap) {
        super(id, componentDbDataMap);
    }
}
