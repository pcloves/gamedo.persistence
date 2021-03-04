package com.gamedo.db;

import com.gamedo.persistence.db.ComponentDbData;
import com.gamedo.persistence.db.DbData;
import com.gamedo.persistence.db.EntityDbData;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document("player")
public class EntityDbPlayer extends EntityDbData {
    public EntityDbPlayer(String id, Map<String, ComponentDbData> componentDbDataMap) {
        super(id, componentDbDataMap);
    }
}
