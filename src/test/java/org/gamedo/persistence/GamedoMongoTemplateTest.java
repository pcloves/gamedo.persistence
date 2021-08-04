package org.gamedo.persistence;

import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.gamedo.persistence.config.MyConfiguration;
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootTest(classes = MyConfiguration.class)
@SpringBootApplication
class GamedoMongoTemplateTest {

    public static final String DEFAULT_NAME = "test";
    final GamedoMongoTemplate gamedoMongoTemplate;

    @Autowired
    GamedoMongoTemplateTest(GamedoMongoTemplate gamedoMongoTemplate) {
        this.gamedoMongoTemplate = gamedoMongoTemplate;
    }

    @BeforeEach
    public void beforeEach() {

        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbPlayer = new EntityDbPlayer(new ObjectId().toString(), null);
        entityDbPlayer.addComponentDbData(new ComponentDbBag(null));
        entityDbPlayer.addComponentDbData(new ComponentDbStatistic(DEFAULT_NAME));

        gamedoMongoTemplate.save(entityDbPlayer);
    }

    @Test
    public void testFindAll() {
        final List<EntityDbPlayer> entityDbPlayerList = gamedoMongoTemplate.findAll(EntityDbPlayer.class);

        Assertions.assertEquals(entityDbPlayerList.size(), 1);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);
        Assertions.assertNotNull(componentDbStatistic);
        Assertions.assertEquals(DEFAULT_NAME, componentDbStatistic.getName());
    }

    @Test
    public void testSave() {
        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));

        gamedoMongoTemplate.save(entityDbData);
    }

    @Test
    public void testSaveAsync() {
        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));

        Assertions.assertDoesNotThrow(() -> gamedoMongoTemplate.saveAsync(entityDbData).get());
    }

    @Test
    public void testUpsert() {
        final List<EntityDbPlayer> entityDbPlayerList = gamedoMongoTemplate.findAll(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);

        componentDbStatistic.setName("hello");
        componentDbStatistic.setDirty("name", componentDbStatistic.getName());

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateFirstAsync(componentDbStatistic);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList1 = gamedoMongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(entityDbPlayerList1.size(), 1);

        final EntityDbPlayer entityDbData1 = entityDbPlayerList1.get(0);
        final ComponentDbStatistic componentDbStatistic1 = entityDbData1.getComponentDbData(ComponentDbStatistic.class);

        Assertions.assertNotNull(componentDbStatistic1);
        Assertions.assertEquals("hello", componentDbStatistic1.getName());
    }

    @Test
    public void testUpsertAsync()
    {
        final List<EntityDbPlayer> entityDbPlayerList = gamedoMongoTemplate.findAll(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);

        componentDbStatistic.setName("hello");
        componentDbStatistic.setDirty("name", componentDbStatistic.getName());
        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateFirstAsync(componentDbStatistic);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getMatchedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
    }

    @Test
    public void testUpsertComponentDbDataWithoutSave() {

        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));
        entityDbData.setComponentDbDataDirty(ComponentDbStatistic.class);

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateFirstAsync(entityDbData);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(0, updateResult.getMatchedCount());
        Assertions.assertEquals(0, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList = gamedoMongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(0, entityDbPlayerList.size());
    }

    @Test
    public void testUpsertAllComponentDbDataWithoutSave() {

        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));
        entityDbData.setAllComponentDbDataDirty();

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateFirstAsync(entityDbData);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(0, updateResult.getMatchedCount());
        Assertions.assertEquals(0, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());
    }

    @Test
    public void testUpsertComponentDbData() {

        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);
        gamedoMongoTemplate.save(entityDbData);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));
        entityDbData.setComponentDbDataDirty(ComponentDbStatistic.class);

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateFirstAsync(entityDbData);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getMatchedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList = gamedoMongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(1, entityDbPlayerList.size());
    }

    @Test
    public void testUpsertAllComponentDbData() {

        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);
        gamedoMongoTemplate.save(entityDbData);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));
        entityDbData.addComponentDbData(new ComponentDbBag(Arrays.asList(1, 2, 3)));
        entityDbData.setAllComponentDbDataDirty();

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateFirstAsync(entityDbData);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getMatchedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList = gamedoMongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(1, entityDbPlayerList.size());
    }
}
