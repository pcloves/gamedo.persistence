package org.gamedo.persistence.listeners;

import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.gamedo.persistence.db.ComponentDbData;
import org.gamedo.persistence.logging.Markers;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;

import java.util.Map;

@Log4j2
public class ComponentDbDataAfterLoadEventListener extends AbstractMongoEventListener<ComponentDbData> {
    @Override
    public void onAfterLoad(AfterLoadEvent<ComponentDbData> event) {
        super.onAfterLoad(event);

        final Document document = event.getSource();
        try {
            final Class<ComponentDbData> clazz = event.getType();

            final Document documentNested = (Document) document.remove(clazz.getSimpleName());
            for (Map.Entry<String, Object> next : documentNested.entrySet()) {
                final String key = next.getKey();
                final Object value = next.getValue();
                document.put(key, value);
            }

            log.debug(Markers.MongoDB, "reading convert finish, document:{}", () -> document);
        } catch (Exception e) {
            log.error(Markers.MongoDB, "exception caught on reading document:" + document, e);
        }
    }

}
