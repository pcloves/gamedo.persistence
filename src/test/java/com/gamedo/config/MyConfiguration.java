package com.gamedo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;

@Slf4j
@Configuration
public class MyConfiguration {

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory("mongodb://127.0.0.1:27017/persistenceTest");
    }

    @Bean
    public MongoTemplate mongoTemplate(final MongoDatabaseFactory databaseFactory, final MongoConverter mongoConverter) {
        return new MongoTemplate(databaseFactory, mongoConverter);
    }
}
