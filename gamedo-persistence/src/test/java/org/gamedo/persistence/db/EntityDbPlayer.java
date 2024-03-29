package org.gamedo.persistence.db;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document("player")
public class EntityDbPlayer extends EntityDbData<String> {
    public EntityDbPlayer(String id, Map<String, ComponentDbData<String>> componentDbDataMap) {
        super(id, componentDbDataMap);
    }
}
