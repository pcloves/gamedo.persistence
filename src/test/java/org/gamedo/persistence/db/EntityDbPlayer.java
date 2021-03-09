package org.gamedo.persistence.db;

import org.gamedo.persistence.persistence.db.ComponentDbData;
import org.gamedo.persistence.persistence.db.EntityDbData;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document("player")
public class EntityDbPlayer extends EntityDbData {
    public EntityDbPlayer(String id, Map<String, ComponentDbData> componentDbDataMap) {
        super(id, componentDbDataMap);
    }
}
