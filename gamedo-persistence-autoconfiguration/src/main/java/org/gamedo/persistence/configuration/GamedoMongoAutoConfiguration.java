package org.gamedo.persistence.configuration;

import org.gamedo.persistence.GamedoMongoTemplate;
import org.gamedo.persistence.listeners.ComponentDbDataAfterLoadEventListener;
import org.gamedo.persistence.listeners.ComponentDbDataBeforeSaveEventListener;
import org.gamedo.persistence.listeners.EntityDbDataAfterLoadEventListener;
import org.gamedo.persistence.listeners.EntityDbDataBeforeSaveEventListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MongoTemplate.class)
@AutoConfigureAfter(MongoDataAutoConfiguration.class)
public class GamedoMongoAutoConfiguration {

    @Bean
    @ConditionalOnBean(MongoTemplate.class)
    @ConditionalOnMissingBean(GamedoMongoTemplate.class)
    GamedoMongoTemplate gamedoMongoTemplate(MongoTemplate mongoTemplate) {
        return new GamedoMongoTemplate(mongoTemplate);
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

    @Bean
    @ConditionalOnBean(MongoConverter.class)
    ComponentDbDataBeforeSaveEventListener componentDbDataBeforeSaveEventListener() {
        return new ComponentDbDataBeforeSaveEventListener();
    }


    @Bean
    @ConditionalOnBean(MongoConverter.class)
    ComponentDbDataAfterLoadEventListener componentDbDataAfterLoadEventListener() {
        return new ComponentDbDataAfterLoadEventListener();
    }
}
