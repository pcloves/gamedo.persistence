package org.gamedo.persistence.config;

import lombok.extern.log4j.Log4j2;
import org.gamedo.persistence.GamedoMongoTemplate;
import org.gamedo.persistence.event.EntityDbDataAfterLoadEventListener;
import org.gamedo.persistence.event.EntityDbDataBeforeSaveEventListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;

@Log4j2
@Configuration
@AutoConfigureAfter(MongoDataAutoConfiguration.class)
public class MyConfiguration {

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory("mongodb://127.0.0.1:27017/persistenceTest");
    }

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory, MongoConverter mongoConverter) {
        return new MongoTemplate(mongoDatabaseFactory, mongoConverter);
    }

    @Bean
    GamedoMongoTemplate gamedoMongoTemplate(MongoTemplate mongoTemplate) {
        return new GamedoMongoTemplate(mongoTemplate);
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
