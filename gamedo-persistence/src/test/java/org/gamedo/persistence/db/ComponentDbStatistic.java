package org.gamedo.persistence.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@Document("player")
public class ComponentDbStatistic extends ComponentDbData<String> {
    private String name;

    public ComponentDbStatistic(String name) {
        this.name = name;
    }
}
