package org.gamedo.persistence;

import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.gamedo.persistence.config.MyConfiguration;
import org.gamedo.persistence.core.DbDataMongoTemplate;
import org.gamedo.persistence.db.ComponentDbBag;
import org.gamedo.persistence.db.ComponentDbStatistic;
import org.gamedo.persistence.db.EntityDbPlayer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
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

    @Test
    public void testFindAll() {
        final List<EntityDbPlayer> entityDbPlayerList = mongoTemplate.findAll(EntityDbPlayer.class);

        Assertions.assertEquals(entityDbPlayerList.size(), 1);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);
        Assertions.assertNotNull(componentDbStatistic);
        Assertions.assertEquals(DEFAULT_NAME, componentDbStatistic.getName());
    }

    @Test
    public void testSave() {
        mongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));

        final EntityDbPlayer entityDbPlayer = Assertions.assertDoesNotThrow(() -> dbDataMongoTemplate.save(entityDbData).get());
    }

    @Test
    public void testSaveAsync() {
        mongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));

        final EntityDbPlayer entityDbPlayer = Assertions.assertDoesNotThrow(() -> dbDataMongoTemplate.saveAsync(entityDbData).get());
    }

    @Test
    public void testUpsert() {
        final List<EntityDbPlayer> entityDbPlayerList = mongoTemplate.findAll(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);

        componentDbStatistic.setName("hello");
        componentDbStatistic.getUpdater().setDirty("name", componentDbStatistic.getName());

        final CompletableFuture<UpdateResult> future = dbDataMongoTemplate.updateFirst(componentDbStatistic);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList1 = mongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(entityDbPlayerList1.size(), 1);

        final EntityDbPlayer entityDbData1 = entityDbPlayerList1.get(0);
        final ComponentDbStatistic componentDbStatistic1 = entityDbData1.getComponentDbData(ComponentDbStatistic.class);

        Assertions.assertNotNull(componentDbStatistic1);
        Assertions.assertEquals("hello", componentDbStatistic1.getName());
    }

    @Test
    public void testUpsertAsync()
    {
        final List<EntityDbPlayer> entityDbPlayerList = mongoTemplate.findAll(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);

        componentDbStatistic.setName("hello");
        componentDbStatistic.getUpdater().setDirty("name", componentDbStatistic.getName());
        final CompletableFuture<UpdateResult> future = dbDataMongoTemplate.updateFirstAsync(componentDbStatistic);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getMatchedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
    }

    @Test
    public void testUpsertComponentDbDataWithoutSave() {

        mongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));
        entityDbData.setComponentDbDataDirty(ComponentDbStatistic.class);

        final CompletableFuture<UpdateResult> future = dbDataMongoTemplate.updateFirst(entityDbData);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(0, updateResult.getMatchedCount());
        Assertions.assertEquals(0, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList = mongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(0, entityDbPlayerList.size());
    }

    @Test
    public void testUpsertAllComponentDbDataWithoutSave() {

        mongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));
        entityDbData.setAllComponentDbDataDirty();

        final CompletableFuture<UpdateResult> future = dbDataMongoTemplate.updateFirst(entityDbData);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(0, updateResult.getMatchedCount());
        Assertions.assertEquals(0, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());
    }

    @Test
    public void testUpsertComponentDbData() {

        mongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);
        mongoTemplate.save(entityDbData);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));
        entityDbData.setComponentDbDataDirty(ComponentDbStatistic.class);

        final CompletableFuture<UpdateResult> future = dbDataMongoTemplate.updateFirst(entityDbData);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getMatchedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList = mongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(1, entityDbPlayerList.size());
    }

    @Test
    public void testUpsertAllComponentDbData() {

        mongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);
        mongoTemplate.save(entityDbData);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));
        entityDbData.addComponentDbData(new ComponentDbBag(Arrays.asList(1, 2, 3)));
        entityDbData.setAllComponentDbDataDirty();

        final CompletableFuture<UpdateResult> future = dbDataMongoTemplate.updateFirst(entityDbData);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getMatchedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList = mongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(1, entityDbPlayerList.size());
    }
}
