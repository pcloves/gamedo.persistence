![GitHub](https://img.shields.io/github/license/pcloves/gamedo.persistence?style=flat-square)![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/pcloves/gamedo.persistence?style=flat-square)![Maven Central](https://img.shields.io/maven-central/v/org.gamedo/persistence?style=flat-square)![GitHub Workflow Status](https://img.shields.io/github/workflow/status/pcloves/gamedo.persistence/Java%20CI%20with%20Maven?style=flat-square)


# gamedo.persistence

gamedo.persistence是gamedo游戏服务器框架的持久化模块。它底层依赖于spring-data-mongodb，致力于构建一个 **高性能、简单易用、易于维护** 的游戏服务器持久化模块。同时，gamedo.persistence吸收了ECS设计模式的思想，也即：“组合优于继承”（这也是开发中的gamedo.ecs模块的一大个特性）。通过对底层的设计，将游戏持久化对象数据约束为Entity-Components（也即一个实体由多个组件组合而成）的形式，从而统一团队成员对于游戏对象数据的规范化设计和使用。

## 开始使用

### Maven配置

增加Maven依赖：

``` xml
<dependency>
  <groupId>org.gamedo</groupId>
  <artifactId>persistence</artifactId>
  <version>1.2.0</version>
</dependency>
```

### 使用说明

1. 定义游戏对象数据使之继承自**EntityDbData**，并使用**@Document**注解来指定该持久化对象数据要持久化到MongoDB的哪一个Document中，一般情况下，该类内不再增加新的成员变量，因为数据应该存储在**ComponentDbData**的子类中

``` java
@Document("player")
public class EntityDbPlayer extends EntityDbData {
    public EntityDbPlayer(String id, Map<String, ComponentDbData> componentDbDataMap) {
        super(id, componentDbDataMap);
    }
}
```

2. 定义若干**ComponentDbData**的子类，并使用和**EntityDbPlayer**相同的**@Document**注解，确保被持久化到同一个Document中，同时该类内定义需要持久化的成员变量

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

3. 定义**EntityDbPlayer**的转换器

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

4. 编写业务逻辑代码

 ``` java
@Slf4j
@SpringBootApplication(scanBasePackages = {"org.gamedo", "org.gamedo.persistence"})
public class Application {
    public static void main(String[] args) {
        final ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);

        //1、从容器中获取DbDataMongoTemplate（否则不具有异步持久化能力）
        final DbDataMongoTemplate dataMongoTemplate = applicationContext.getBean(DbDataMongoTemplate.class);
        //2、创建一个EntityDbData
        final EntityDbPlayer entityDbPlayer = new EntityDbPlayer(new ObjectId().toString(), null);

        //3、增加一个组件数据：ComponentDbData
        entityDbPlayer.addComponentDbData(new ComponentDbBag(new ArrayList<>()));

        //4、调用同步save函数，将完整的EntityDbPlayer持久化到MongoDB中，同样可以调用dataMongoTemplate.saveAsync(entityDbPlayer)
        //实现异步存储，可以参考接下来的示例
        dataMongoTemplate.save(entityDbPlayer);

        //----------------------------------------------------------------------

        //5、获取组件数据
        final ComponentDbBag componentDbData = entityDbPlayer.getComponentDbData(ComponentDbBag.class);
        //6、修改组件数据
        componentDbData.getItemList().add(1);
        //7、对修改的成员变量进行标脏
        componentDbData.getUpdater().setDirty("itemList", componentDbData.getItemList());

        //8、通过CompletableFuture检查执行结果
        final CompletableFuture<UpdateResult> future = dataMongoTemplate.updateFirstAsync(componentDbData);
        future.whenCompleteAsync((result, t) -> {
            if (t != null) {
                log.error("", t);
            } else {
                log.info("update async finish, result:{}", result);
            }

            applicationContext.close();
        });

        log.info("Application run finish.");
    }
}
 ```

当执行完第4步后，MongoDB中数据为：

``` 
{ 
    "_id" : ObjectId("604f485ce3d58d6ff1fd6f1f"), 
    "_class" : "org.gamedo.db.EntityDbPlayer", 
    "ComponentDbBag" : {
        "itemList" : [

        ], 
        "_class" : "org.gamedo.db.ComponentDbBag"
    }
}
```

第5~8步骤属于gamedo.persistence提供的**异步**的**局部增量更新**的特性，当程序运行结束后，MongoDB中数据为：

``` 
{ 
    "_id" : ObjectId("60505a7b35f0b86566a75193"), 
    "_class" : "org.gamedo.db.EntityDbPlayer", 
    "ComponentDbBag" : {
        "itemList" : [
            NumberInt(1)
        ], 
        "_class" : "org.gamedo.db.ComponentDbBag"
    }
}
```

通过对比可知，文档中仅仅ComponentDbBag.itemList发生了变化。控制台日志输出为：

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

由于gamedo.persistence模块的底层数据库是MongoDB，而后者提供了“$set”操作符，这允许用户可以对MongoDB中文档（甚至是内嵌的文档）的字段进行局部更新，gamedo.persistence正是利用了这个特性，设计了一个线程安全的Updater，为每一个DbData配备了一个独立的Updater， 并通过一系列的封装和设计，使之具有简单易用的特性。此外Updater使用了spring-data-mongodb的Update，当安全地发布到db线程后，不需要执行反序列化操作，可以直接执行持久化操作。也将前文所说的全局序列化/反序列化的操作拆分为一个个局部的序列化，