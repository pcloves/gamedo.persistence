package org.gamedo.persistence;

import com.mongodb.client.result.UpdateResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.gamedo.persistence.config.MyConfiguration;
import org.gamedo.persistence.db.ComponentDbBag;
import org.gamedo.persistence.db.ComponentDbStatistic;
import org.gamedo.persistence.db.EntityDbPlayer;
import org.gamedo.persistence.persistence.core.DbDataMongoTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootTest(classes = {MyConfiguration.class})
@SpringBootApplication
class DbDataMongoTemplateTest {

    public static final String DEFAULT_NAME = "test";
    final MongoTemplate mongoTemplate;
    final DbDataMongoTemplate dbDataMongoTemplate;

    @Autowired
    DbDataMongoTemplateTest(MongoTemplate mongoTemplate, DbDataMongoTemplate dbDataMongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.dbDataMongoTemplate = dbDataMongoTemplate;
    }

    @BeforeEach
    public void beforeEach() {

        mongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbPlayer = new EntityDbPlayer(new ObjectId().toString(), null);
        entityDbPlayer.addComponentDbData(new ComponentDbBag(null));
        entityDbPlayer.addComponentDbData(new ComponentDbStatistic(DEFAULT_NAME));

        final CompletableFuture<EntityDbPlayer> future = dbDataMongoTemplate.save(entityDbPlayer);
        future.whenComplete((player, throwable) -> {
            if (throwable != null) {
                log.error("exception caught.", throwable);
            } else {
                log.info("save finish, player:{}", player);
            }
        });
    }
    @SneakyThrows
    @Test
    public void testSave() {
        final List<EntityDbPlayer> entityDbPlayerList = mongoTemplate.findAll(EntityDbPlayer.class);

        Assertions.assertEquals(entityDbPlayerList.size(), 1);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);
        Assertions.assertNotNull(componentDbStatistic);
        Assertions.assertEquals(DEFAULT_NAME, componentDbStatistic.getName());
    }

    @SneakyThrows
    @Test
    public void testIncrementalUpdate() {
        final List<EntityDbPlayer> entityDbPlayerList = mongoTemplate.findAll(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);

        componentDbStatistic.setName("hello");
        componentDbStatistic.getUpdate().set("name", componentDbStatistic.getName());

        final CompletableFuture<UpdateResult> future = dbDataMongoTemplate.upsert(componentDbStatistic);
        Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        final UpdateResult updateResult = future.get();
        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList1 = mongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(entityDbPlayerList1.size(), 1);


        final EntityDbPlayer entityDbData1 = entityDbPlayerList1.get(0);
        final ComponentDbStatistic componentDbStatistic1 = entityDbData1.getComponentDbData(ComponentDbStatistic.class);

        Assertions.assertNotNull(componentDbStatistic1);
        Assertions.assertEquals(componentDbStatistic1.getName(), "hello");
    }
}
