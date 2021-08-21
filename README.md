![GitHub](https://img.shields.io/github/license/pcloves/gamedo.persistence?style=flat-square) ![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/pcloves/gamedo.persistence?style=flat-square) ![Maven Central](https://img.shields.io/maven-central/v/org.gamedo/persistence?style=flat-square) ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/pcloves/gamedo.persistence/Java%20CI%20with%20Maven?style=flat-square)


# gamedo.persistence

gamedo.persistence是gamedo游戏服务器框架的持久化模块。它底层依赖于spring-data-mongodb，致力于构建一个 **高性能、简单易用、易于维护** 的游戏服务器持久化模块。同时，gamedo.persistence吸收了ECS设计模式的思想，也即：“组合优于继承”（这也是处于开发中的[gamedo.core](https://github.com/pcloves/gamedo.core) 模块的一大个特性）。通过对底层的封装，将游戏持久化对象数据约束为Entity-Components（也即一个实体由多个组件组合而成）的形式，从而统一团队成员对于游戏对象数据的规范化设计和使用。

## 开始使用

### Maven配置

增加Maven依赖：

``` xml
<dependency>
  <groupId>org.gamedo</groupId>
  <artifactId>persistence</artifactId>
  <version>${gamedo.persistence.version}</version>
</dependency>
```

### 使用说明

1. 定义游戏对象数据使之继承自**EntityDbData**，并使用 **@Document**注解来指定该持久化对象数据要持久化到MongoDB的哪一个Document中，一般情况下，该类内不再增加新的成员变量，因为数据应该存储在**ComponentDbData**的子类中，例如定义个玩家类：

``` java
@Document("player")
public class EntityDbPlayer extends EntityDbData {
    public EntityDbPlayer(String id, Map<String, ComponentDbData> componentDbDataMap) {
        super(id, componentDbDataMap);
    }
}
```

2. 根据开发需求，定义不同的组件数据类，也即是 **ComponentDbData**的子类，并使用和**EntityDbPlayer**相同的 **@Document**注解，确保被持久化到同一个Document中，例如定义一个背包类

``` java
@EqualsAndHashCode(callSuper = true)
@Data
@Document("player")
public class ComponentDbBag extends ComponentDbData
{
    final List<Integer> itemList = new ArrayList<>();

    public ComponentDbBag(final List<Integer> itemList) {
        this.itemList.addAll(itemList != null ? itemList : Collections.emptyList());
    }
}
```

3. 定义**EntityDbPlayer**的转换器（属于样板代码，不需要实现逻辑），由于gamedo.persistence在设计过程中，使用了使用了自定义转换器对EntityDbData类进行序列化和反序列化，因此其子类也需要继承 **AbstractEntityDbDataReadingConverter**和 **AbstractEntityDbDataWritingConverter**并加上 **@Component** 注解，目的可以正确加载到spring的IOC容器中

``` java
@Component
@ReadingConverter
public class EntityDbPlayerReadingConverter extends AbstractEntityDbDataReadingConverter<EntityDbPlayer> {
    public EntityDbPlayerReadingConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}

@Component
@WritingConverter
public class EntityDbPlayerWriterConverter extends AbstractEntityDbDataWritingConverter<EntityDbPlayer> {
    public EntityDbPlayerWriterConverter(MongoConfiguration configuration) {
        super(configuration);
    }
}
```

4. 搞定！接下来就是使用gamedo.persistence了

 ``` java
@Slf4j
@SpringBootApplication(scanBasePackages = {"org.gamedo", "org.gamedo.persistence"})
public class Application {
    public static void main(String[] args) {
        final ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);

        //1、从容器中获取GamedoMongoTemplate
        final GamedoMongoTemplate dataMongoTemplate = applicationContext.getBean(GamedoMongoTemplate.class);

        //2、创建一个玩家持久化对象类：EntityDbPlayer
        final EntityDbPlayer entityDbPlayer = new EntityDbPlayer(new ObjectId().toString(), null);
        //2.1、为该持久化对象数据增加一个组件数据：ComponentDbData
        entityDbPlayer.addComponentDbData(new ComponentDbBag(new ArrayList<>()));

        //3、调用同步save函数，将完整的EntityDbPlayer持久化到MongoDB中，同样可以调用dataMongoTemplate.saveAsync(entityDbPlayer)
        //实现异步存储，可以参考接下来的示例，当然，仍然可以使用spring-data-mongodb提供的MongoTemplate也是可以的
        dataMongoTemplate.save(entityDbPlayer);
        //3.1、调用异步save函数，该方法会把entityDbPlayer安全地发布到db线程后，就直接返回，真正io操作在db线程执行（如果使用不指定线程池的重载接口，默认使用ForkJoinPool.commonPool()线程池）。
        dataMongoTemplate.saveAsync(entityDbPlayer);

        //接下来是gamedo.persistence提供的增量更新功能------------------------------------------------------------------

        //4、获取组件数据
        final ComponentDbBag componentDbData = entityDbPlayer.getComponentDbData(ComponentDbBag.class);
        //5、修改数据
        componentDbData.getItemList().add(1);
        //6、对修改的变量进行标脏
        componentDbData.setDirty("itemList", componentDbData.getItemList());
        //7、进行异步更新，并通过CompletableFuture检查执行结果（如果使用不指定线程池的重载接口，默认使用ForkJoinPool.commonPool()线程池）。
        dataMongoTemplate.updateFirstAsync(null)
                         .exceptionally(throwable -> {
                                log.error("exception caught.", throwable);
                                return UpdateResult.unacknowledged();
                         })
                         .thenAccept(t -> {
                                log.info("update async finish, result:{}", t);
                                applicationContext.close();
                         });

        log.info("application run finish.");
    }
}
 ```

当执行完第3.1步后，MongoDB中数据为：

``` 
{ 
    "_id" : ObjectId("604f485ce3d58d6ff1fd6f1f"), 
    "_class" : "org.gamedo.db.EntityDbPlayer", 
    "ComponentDbBag" : {
        "itemList" : [], 
        "_class" : "org.gamedo.db.ComponentDbBag"
    }
}
```

第4~7步属于gamedo.persistence提供的**异步**的**局部增量更新**的特性，在团队协作开发中，一般会把第7步封装到一个单独的模块中，进行统一的持久化操作，比如每隔5秒检查 **DbData.isDirty()**，并进行异步局部增量更新，而对于上层使用者只需要进行标脏操作即可。

当程序运行结束后，MongoDB中数据为：

``` 
{ 
    "_id" : ObjectId("604f485ce3d58d6ff1fd6f1f"), 
    "_class" : "org.gamedo.db.EntityDbPlayer", 
    "ComponentDbBag" : {
        "itemList" : [ NumberInt(1) ], 
        "_class" : "org.gamedo.db.ComponentDbBag"
    }
}
```

通过对比可知，文档中仅仅ComponentDbBag.itemList里增加了一个NumberInt(1)，而控制台日志输出为：

``` 
2021-03-16 15:21:05.265  INFO 36044 --- [           main] org.gamedo.Application                   : application run finish.
2021-03-16 15:21:05.275  INFO 36044 --- [nPool-worker-19] org.gamedo.Application                   : updateFirstAsync finish, result:AcknowledgedUpdateResult{matchedCount=1, modifiedCount=1, upsertedId=null}
```

通过日志可知：

1. 持久化发生在nPool-worker-19线程，而非main主线程
2. 持久化日志在主线程日志之后打印，也即不会阻塞main主线程的业务逻辑

## 设计思想

### ECS：组合优于继承

在gamedo.persistence中，游戏持久化对象数据被定义为：EntityDbData。它对应于ECS中的E（Entity），并且和ECS中的Entity相同的是：EntityDbData仅仅只是一个数据容器（当然，如果非得在EntityDbData的子类内增加成员变量也是可以的），真正要存储的数据都放在ComponentDbData中，对应于ECS中的C（Component），同时，EntityDbData和ComponentDbData都实现了DbData接口，通过观察DbData接口的定义，可以知道：

* 它包含一个String类型的Id，映射到MongoDB的Document的_id字段
* 它包含一个更新器，代表着这个DbData内的属性都可以通过$set操作符进标脏

在gamedo.persistence的实现中，EntityDbData中含有一个Map<String, ComponentDbData>的成员变量，其中Key为ComponentDbData所代表的java.lang.Class的getSimpleName()返回值，Value为ComponentDbData的子类实现。DbData、EntityDbData、ComponentDbData的继承关系如图所示：

![gamedo.persistence.DbData-UML.png](https://raw.githubusercontent.com/pcloves/gamedo.persistence/main/images/gamedo.persistence.DbData-UML.png)

### 高性能：化整为零

在游戏服务器框架的持久化业务场景中，一般情况都是游戏逻辑线程负责对游戏持久化对象数据进行修改，而在另外一个线程（以下简称为db线程）对该持久化数据进行读操作，并将之持久化到db中。这样读写分离的操作是为了带来更好的性能，假设对于持久化数据的修改和持久化操作都在业务线程中，这将会极大地影响业务线程。而读写操作不在同一个线程也带来了java编程中最常见的多线程问题：内存可见性和并发竞争。总而言之，就是要解决如何将游戏持久化对象数据安全地发布到db线程中，一种经典的做法是先将持久化对象数据序列化成中间状态，发布到db线程后再反序列化为原来状态后执行持久化操作。而这种操作带来了一个缺点，就是：带来了无谓的性能损耗。当持久化对象数据非常大时，这种性能损耗将会更加明显，即使仅修改一个简单的成员变量，也要执行全局序列化/反序列化。

由于gamedo.persistence模块的底层数据库是MongoDB，而后者提供了 **“$set”** 操作符，这允许用户可以对MongoDB中文档（甚至是内嵌的文档）的字段进行局部更新，gamedo.persistence正是利用了这个特性，设计了一个线程安全的更新器（Updater），并为每个Entity和Component配备了一个独立的Updater， 并通过一系列的封装和设计，使之具有简单易用的特性。此外Updater内部使用了spring-data-mongodb的Update，当安全地发布到db线程后，可以直接执行持久化操作，而不会带来额外的性能反序列化性能开销。这种化整为零的拆分思想保证了只有需要更新的字段才会进行持久化操作，而无需进行无谓的全局序列化/反序列化。

### Benchmark

先上一张很随意的结论图，更加详细的测试报告稍后添加：

![gamedo.persistence.DbData-UML.png](https://raw.githubusercontent.com/pcloves/gamedo.persistence/main/images/jmh-result.png)