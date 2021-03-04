package com.gamedo.db;

import com.gamedo.persistence.db.ComponentDbData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Document("player")
public class ComponentDbBag extends ComponentDbData
{
    final List<Integer> itemList = new ArrayList<>();

    public ComponentDbBag(final List<Integer> itemList) {
        this.itemList.addAll(itemList != null ? itemList : Collections.emptyList());
    }
}
