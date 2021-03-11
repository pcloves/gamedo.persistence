package org.gamedo.persistence.config;

import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class MongoConfiguration {

    @Getter
    private final MongoConverter mongoConverter;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public MongoConfiguration(final MongoDatabaseFactory factory) {
        this.mongoConverter = mongoConverter(factory);
    }

    /**
     * inner {@link MongoConverter} for {@link Converter Converter}
     *
     * @param factory the MongoDatabaseFactory
     * @return MongoConverter
     */
    private MongoConverter mongoConverter(final MongoDatabaseFactory factory) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MongoCustomConversions conversions = new MongoCustomConversions(Collections.emptyList());

        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        mappingContext.afterPropertiesSet();

        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(conversions);
        converter.setCodecRegistryProvider(factory);
        converter.afterPropertiesSet();

        return converter;
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public MongoCustomConversions mongoCustomConversions(Converter<?, ?>... converters) {
        return new MongoCustomConversions(Arrays.asList(converters));
    }
}
