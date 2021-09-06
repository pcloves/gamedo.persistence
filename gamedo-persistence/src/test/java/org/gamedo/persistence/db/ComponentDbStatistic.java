package org.gamedo.persistence.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.gamedo.persistence.annotations.EntityDbDataComponent;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@Document("player")
@EntityDbDataComponent(EntityDbPlayer.class)
public class ComponentDbStatistic extends ComponentDbData {
    private String name;

    public ComponentDbStatistic(String name) {
        this.name = name;
    }
}
