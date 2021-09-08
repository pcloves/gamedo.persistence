package org.gamedo.persistence;

import com.mongodb.client.result.UpdateResult;
import lombok.extern.log4j.Log4j2;
import org.bson.types.ObjectId;
import org.gamedo.persistence.config.MyConfiguration;
import org.gamedo.persistence.db.ComponentDbBag;
import org.gamedo.persistence.db.ComponentDbComplex;
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Log4j2
@SpringBootTest(classes = MyConfiguration.class)
@SpringBootApplication
class GamedoMongoTemplateTest {

    public static final String EntityId = new ObjectId().toString();
    public static final String DEFAULT_NAME = "test";
    final GamedoMongoTemplate gamedoMongoTemplate;

    @Autowired
    GamedoMongoTemplateTest(GamedoMongoTemplate gamedoMongoTemplate) {
        this.gamedoMongoTemplate = gamedoMongoTemplate;
    }

    @BeforeEach
    public void beforeEach() {

        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbPlayer = new EntityDbPlayer(EntityId, null);
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

        final ComponentDbStatistic componentDbData = new ComponentDbStatistic("componentDbData");
        entityDbData.addComponentDbData(componentDbData);

        Assertions.assertDoesNotThrow(() -> gamedoMongoTemplate.save(entityDbData));

        componentDbData.setName("test");
        Assertions.assertDoesNotThrow(() -> gamedoMongoTemplate.save(componentDbData));

        final ComponentDbStatistic data = gamedoMongoTemplate.findComponentDbDataByIdAsync(entityDbData.id, ComponentDbStatistic.class).join();
        Assertions.assertEquals("test", data.getName());
    }

    @Test
    public void testSaveAsync() {
        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        final ComponentDbStatistic componentDbStatistic = new ComponentDbStatistic("testSaveComponentDbData");
        entityDbData.addComponentDbData(componentDbStatistic);

        Assertions.assertDoesNotThrow(() -> gamedoMongoTemplate.saveDbDataAsync(entityDbData).get());
        Assertions.assertDoesNotThrow(() -> gamedoMongoTemplate.saveDbDataAsync(componentDbStatistic).get());
    }

    @Test
    public void testUpsert() {
        final List<EntityDbPlayer> entityDbPlayerList = gamedoMongoTemplate.findAll(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);

        componentDbStatistic.setName("hello");
        componentDbStatistic.update("name", componentDbStatistic.getName());

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateDbDataFirstAsync(componentDbStatistic);
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
    public void testUpsertAsync() {
        final List<EntityDbPlayer> entityDbPlayerList = gamedoMongoTemplate.findAll(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = entityDbPlayerList.get(0);
        final ComponentDbStatistic componentDbStatistic = entityDbData.getComponentDbData(ComponentDbStatistic.class);

        componentDbStatistic.setName("hello");
        componentDbStatistic.update("name", componentDbStatistic.getName());
        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateDbDataFirstAsync(componentDbStatistic);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getMatchedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
    }

    @Test
    public void testUpsertComponentDbDataWithoutSave() {

        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final EntityDbPlayer entityDbData = new EntityDbPlayer(new ObjectId().toString(), null);

        entityDbData.addComponentDbData(new ComponentDbStatistic("testSaveComponentDbData"));

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateDbDataFirstAsync(entityDbData);
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
        entityDbData.updateAllComponentDbData();

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateDbDataFirstAsync(entityDbData);
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
        entityDbData.updateComponentDbData(ComponentDbStatistic.class);

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateDbDataFirstAsync(entityDbData);
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
        entityDbData.updateAllComponentDbData();

        final CompletableFuture<UpdateResult> future = gamedoMongoTemplate.updateDbDataFirstAsync(entityDbData);
        final UpdateResult updateResult = Assertions.assertDoesNotThrow((ThrowingSupplier<UpdateResult>) future::get);

        Assertions.assertEquals(1, updateResult.getMatchedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());
        Assertions.assertNull(updateResult.getUpsertedId());

        final List<EntityDbPlayer> entityDbPlayerList = gamedoMongoTemplate.findAll(EntityDbPlayer.class);
        Assertions.assertEquals(1, entityDbPlayerList.size());
    }

    @Test
    public void testFindComponentDbDataDbDataByIdAsync() {
        final CompletableFuture<ComponentDbStatistic> future = gamedoMongoTemplate.findComponentDbDataByIdAsync(EntityId, ComponentDbStatistic.class);

        future.thenAccept(componentDbStatistic -> Assertions.assertEquals(DEFAULT_NAME, componentDbStatistic.getName())).join();

    }

    @Test
    public void testComplexData() {
        gamedoMongoTemplate.dropCollection(EntityDbPlayer.class);

        final ComponentDbComplex componentDbComplex = ComponentDbComplex.builder().build();
        componentDbComplex.setId(new ObjectId().toString());

        gamedoMongoTemplate.save(componentDbComplex);

        final ComponentDbComplex componentDbComplexLoad1 = gamedoMongoTemplate.findComponentDbDataByIdAsync(componentDbComplex.getId(), ComponentDbComplex.class).join();
        Assertions.assertEquals(componentDbComplex, componentDbComplexLoad1);

        final byte[] bytes = new byte[2];
        ThreadLocalRandom.current().nextBytes(bytes);

        componentDbComplex.setBooleanValue(ThreadLocalRandom.current().nextBoolean());
        componentDbComplex.setByteValue(bytes[0]);
        componentDbComplex.setShortValue((short) ThreadLocalRandom.current().nextInt(Short.MAX_VALUE + 1));
        componentDbComplex.setIntValue(ThreadLocalRandom.current().nextInt());
        componentDbComplex.setLongValue(ThreadLocalRandom.current().nextLong());

        componentDbComplex.setBooleanBoxedValue(ThreadLocalRandom.current().nextBoolean());
        componentDbComplex.setByteBoxedValue(bytes[1]);
        componentDbComplex.setShortBoxedValue((short) ThreadLocalRandom.current().nextInt(Short.MAX_VALUE + 1));
        componentDbComplex.setIntBoxedValue(ThreadLocalRandom.current().nextInt());
        componentDbComplex.setLongBoxedValue(ThreadLocalRandom.current().nextLong());

        componentDbComplex.setStringValue(randomString(ThreadLocalRandom.current().nextInt(100)));
        componentDbComplex.setDateValue(new Date(ThreadLocalRandom.current().nextLong()));

        componentDbComplex.setInnerDataSet(IntStream.range(1, ThreadLocalRandom.current().nextInt(30))
                .mapToObj(k -> randomInnerData())
                .collect(Collectors.toSet()));

        gamedoMongoTemplate.save(componentDbComplex);
        final ComponentDbComplex componentDbComplexLoad2 = gamedoMongoTemplate.findComponentDbDataByIdAsync(componentDbComplex.getId(), ComponentDbComplex.class).join();
        Assertions.assertEquals(componentDbComplex, componentDbComplexLoad2);

        componentDbComplex.setStringValue(randomString(100));
        componentDbComplex.update("stringValue", componentDbComplex.getStringValue());
        componentDbComplex.update("innerDataSet", componentDbComplex.getInnerDataSet());

        final UpdateResult updateResult = gamedoMongoTemplate.updateDbDataFirstAsync(componentDbComplex).join();
        Assertions.assertEquals(1, updateResult.getMatchedCount());
        Assertions.assertEquals(1, updateResult.getModifiedCount());

        final ComponentDbComplex componentDbComplexLoad3 = gamedoMongoTemplate.findComponentDbDataByIdAsync(componentDbComplex.getId(), ComponentDbComplex.class).join();
        Assertions.assertEquals(componentDbComplex.getStringValue(), componentDbComplexLoad3.getStringValue());
    }

    ComponentDbComplex.InnerData randomInnerData() {
        final byte[] bytes = new byte[2];
        ThreadLocalRandom.current().nextBytes(bytes);
        return ComponentDbComplex.InnerData.builder()

                .booleanValue(ThreadLocalRandom.current().nextBoolean())
                .byteValue(bytes[0])
                .shortValue((short) ThreadLocalRandom.current().nextInt(Short.MAX_VALUE + 1))
                .intValue(ThreadLocalRandom.current().nextInt())
                .longValue(ThreadLocalRandom.current().nextLong())

                .booleanBoxedValue(ThreadLocalRandom.current().nextBoolean())
                .byteBoxedValue(bytes[1])
                .shortBoxedValue((short) ThreadLocalRandom.current().nextInt(Short.MAX_VALUE + 1))
                .intBoxedValue(ThreadLocalRandom.current().nextInt())
                .longBoxedValue(ThreadLocalRandom.current().nextLong())

                .stringValue(randomString(ThreadLocalRandom.current().nextInt(100)))
                .dateValue(new Date(ThreadLocalRandom.current().nextLong()))

                .innerInnerDataSet(IntStream.rangeClosed(1, ThreadLocalRandom.current().nextInt(30))
                        .mapToObj(k -> randomInnerInnerData())
                        .collect(Collectors.toSet()))

                .longInnerInnerDataMap(LongStream.range(1, ThreadLocalRandom.current().nextInt(30))
                        .boxed()
                        .collect(Collectors.toMap(Function.identity(), key -> randomInnerInnerData())))
                .build();
    }

    ComponentDbComplex.InnerInnerData randomInnerInnerData() {
        final byte[] bytes = new byte[2];
        ThreadLocalRandom.current().nextBytes(bytes);

        return ComponentDbComplex.InnerInnerData.builder()
                .booleanValue(ThreadLocalRandom.current().nextBoolean())
                .byteValue(bytes[0])
                .shortValue((short) ThreadLocalRandom.current().nextInt(Short.MAX_VALUE + 1))
                .intValue(ThreadLocalRandom.current().nextInt())
                .longValue(ThreadLocalRandom.current().nextLong())

                .booleanBoxedValue(ThreadLocalRandom.current().nextBoolean())
                .byteBoxedValue(bytes[1])
                .shortBoxedValue((short) ThreadLocalRandom.current().nextInt(Short.MAX_VALUE + 1))
                .intBoxedValue(ThreadLocalRandom.current().nextInt())
                .longBoxedValue(ThreadLocalRandom.current().nextLong())

                .stringValue(randomString(ThreadLocalRandom.current().nextInt(100)))
                .dateValue(new Date(ThreadLocalRandom.current().nextLong()))

                .booleanSet(new HashSet<>(List.of(ThreadLocalRandom.current().nextBoolean())))
                .shortSet(IntStream.generate(() -> ThreadLocalRandom.current().nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1))
                        .limit(ThreadLocalRandom.current().nextInt(30, ThreadLocalRandom.current().nextInt(31, 101)))
                        .mapToObj(i -> (short) i)
                        .collect(Collectors.toSet()))
                .integerSet(IntStream.generate(() -> ThreadLocalRandom.current().nextInt())
                        .limit(ThreadLocalRandom.current().nextInt(30, ThreadLocalRandom.current().nextInt(31, 101)))
                        .boxed()
                        .collect(Collectors.toSet()))
                .longSet(LongStream.generate(() -> ThreadLocalRandom.current().nextInt())
                        .limit(ThreadLocalRandom.current().nextInt(30, ThreadLocalRandom.current().nextInt(31,101)))
                        .boxed()
                        .collect(Collectors.toSet()))
                .longLongMap(LongStream.generate(() -> ThreadLocalRandom.current().nextLong())
                        .limit(ThreadLocalRandom.current().nextInt(30, ThreadLocalRandom.current().nextInt(31, 101)))
                        .boxed()
                        .collect(Collectors.toMap(Function.identity(), Function.identity())))
                .longStringMap(LongStream.generate(() -> ThreadLocalRandom.current().nextLong())
                        .limit(ThreadLocalRandom.current().nextInt(30, ThreadLocalRandom.current().nextInt(31, 101)))
                        .boxed()
                        .collect(Collectors.toMap(Function.identity(), key -> randomString(ThreadLocalRandom.current().nextInt(100)))))
                .build();

    }

    String randomString(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'

        return ThreadLocalRandom.current().ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
