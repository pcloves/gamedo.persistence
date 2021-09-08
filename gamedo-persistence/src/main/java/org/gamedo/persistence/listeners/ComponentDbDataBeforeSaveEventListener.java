package org.gamedo.persistence.listeners;

import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.gamedo.persistence.db.ComponentDbData;
import org.gamedo.persistence.logging.Markers;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class ComponentDbDataBeforeSaveEventListener extends AbstractMongoEventListener<ComponentDbData> {

    @Override
    public void onBeforeSave(BeforeSaveEvent<ComponentDbData> event) {
        super.onBeforeSave(event);

        final ComponentDbData source = event.getSource();
        final Document document = event.getDocument();
        final Iterator<Map.Entry<String, Object>> iterator = Objects.requireNonNull(document).entrySet().iterator();
        final Document documentNested = new Document();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> next = iterator.next();
            final String key = next.getKey();
            final Object value = next.getValue();
            if (!key.equals("_id")) {
                documentNested.put(key, value);
                iterator.remove();
            }
        }

        document.put(source.getClass().getSimpleName(), documentNested);

        log.debug(Markers.MongoDB, "writing convert finish, source:{}, target:{}", () -> source, () -> document);
    }
}
