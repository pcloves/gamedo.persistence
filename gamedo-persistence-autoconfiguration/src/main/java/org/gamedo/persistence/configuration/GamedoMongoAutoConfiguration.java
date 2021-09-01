package org.gamedo.persistence.configuration;

import org.gamedo.persistence.GamedoMongoTemplate;
import org.gamedo.persistence.event.EntityDbDataAfterLoadEventListener;
import org.gamedo.persistence.event.EntityDbDataBeforeSaveEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MongoTemplate.class)
public class GamedoMongoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({MongoOperations.class})
    @ConditionalOnBean({MongoDatabaseFactory.class, MongoConverter.class})
    GamedoMongoTemplate gamedoMongoTemplate(MongoDatabaseFactory factory, MongoConverter converter) {
        return new GamedoMongoTemplate(factory, converter);
    }

    @Bean
    @ConditionalOnBean(MongoConverter.class)
    EntityDbDataBeforeSaveEventListener entityDbDataBeforeSaveEventListener(MongoConverter mongoConverter) {
        return new EntityDbDataBeforeSaveEventListener(mongoConverter);
    }

    @Bean
    @ConditionalOnBean(MongoConverter.class)
    EntityDbDataAfterLoadEventListener entityDbDataAfterLoadEventListener(MongoConverter mongoConverter) {
        return new EntityDbDataAfterLoadEventListener(mongoConverter);
    }
}
