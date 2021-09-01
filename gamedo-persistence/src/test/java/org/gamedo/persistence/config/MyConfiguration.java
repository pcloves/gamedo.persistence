package org.gamedo.persistence.config;

import lombok.extern.log4j.Log4j2;
import org.gamedo.persistence.GamedoMongoTemplate;
import org.gamedo.persistence.event.EntityDbDataAfterLoadEventListener;
import org.gamedo.persistence.event.EntityDbDataBeforeSaveEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;

@Log4j2
@Configuration
public class MyConfiguration {

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory("mongodb://127.0.0.1:27017/persistenceTest");
    }

    @Bean
    @ConditionalOnMissingBean(GamedoMongoTemplate.class)
    GamedoMongoTemplate gamedoMongoTemplate(MongoDatabaseFactory factory, MongoConverter converter) {
        return new GamedoMongoTemplate(factory, converter);
    }

    @Bean
    EntityDbDataBeforeSaveEventListener entityDbDataBeforeSaveEventListener(MongoConverter mongoConverter) {
        return new EntityDbDataBeforeSaveEventListener(mongoConverter);
    }

    @Bean
    EntityDbDataAfterLoadEventListener entityDbDataAfterLoadEventListener(MongoConverter mongoConverter) {
        return new EntityDbDataAfterLoadEventListener(mongoConverter);
    }
}
