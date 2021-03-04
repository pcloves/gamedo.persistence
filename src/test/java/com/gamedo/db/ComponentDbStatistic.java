package com.gamedo.db;

import com.gamedo.persistence.db.ComponentDbData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@Document("player")
public class ComponentDbStatistic extends ComponentDbData {
    private String name;

    public ComponentDbStatistic(String name) {
        this.name = name;
    }
}
