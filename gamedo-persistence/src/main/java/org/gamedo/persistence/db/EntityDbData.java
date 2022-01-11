package org.gamedo.persistence.db;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.gamedo.persistence.GamedoMongoTemplate;
import org.gamedo.persistence.annotations.ComponentMap;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link EntityDbData}代表一个实体的存储数据（以后统称该类为实体存储类），一般情况下该实体不包含实质性的存储数据，要存储的数据都以组件的形式
 * {@link EntityDbData#addComponentDbData(ComponentDbData)}加到本实体存储类中。在实际的持久化中，gamedo-persistence使用了一些小策
 * 略：并没有按照实体存储类的原本类布局进行存储，而是将{@link EntityDbData#componentDbDataMap}打散，然后将各个组件进行存储，假如有一个实体
 * 存储类：EntityDbPlayer，对应的数据库名为：player，该实体存储类包含一个组件：ComponentDbPosition，那么当进行持久化之后，player数据库中
 * 一条文档记录为：
 * <pre>
 * {
 *     "_id" : "gamedo-25",
 *     "_class" : "org.gamedo.demo.persistence.EntityDbPlayer",
 *     "ComponentDbPosition" : {
 *         "x" : NumberInt(32),
 *         "y" : NumberInt(47),
 *         "_class" : "org.gamedo.demo.persistence.ComponentDbPosition"
 *     }
 * }
 * </pre>
 * 可以看出{@link EntityDbData#componentDbDataMap}并不存在，且已经被打散，这么做带来了一个优势：就是除了可以加载完整数据，也可以独立加载任
 * 意一个组件数据（使用{@link GamedoMongoTemplate#findComponentDbDataByIdAsync(Object, Class)}提供的方
 * 法），同时组件也可以独立存储（使用{@link GamedoMongoTemplate#saveDbDataAsync(DbData)}）
 * @param <I> 主键的类型
 */
@SuppressWarnings("unused")
@Data
@EqualsAndHashCode(of = {"id", "componentDbDataMap"})
public class EntityDbData<I> implements DbData<I> {
    /**
     * 映射到mongoDB的_id字段
     */
    @Id
    public volatile I id;
    /**
     * 组件所属的{@link Class}的简化名称（{@link Class#getSimpleName()}）到组件的映射
     */
    @Getter(AccessLevel.NONE)
    @ComponentMap
    private Map<String, ComponentDbData<I>> componentDbDataMap;
    /**
     * 当前所属更新器
     */
    @Transient
    private transient volatile IUpdater updater;

    public EntityDbData() {
       this(null, null);
    }

    public EntityDbData(final I id) {
        this(id, null);
    }

    @PersistenceConstructor
    public EntityDbData(final I id, final Map<String, ComponentDbData<I>> componentDbDataMap) {
        this.id = id;
        this.componentDbDataMap = new HashMap<>(componentDbDataMap == null ? Collections.emptyMap() : componentDbDataMap);
        this.componentDbDataMap.forEach((s, dbData) -> dbData.setId(this.id));

        updater = new Updater("");
    }

    /**
     * 添加一个组件数据到本实体中
     * @param dbData 要添加的组件数据
     * @param <T> 要添加的组件类型
     * @return 如果要添加的组件已经设置了id，则添加失败
     * @throws RuntimeException 如果要添加的组件已经设置了id，则抛出异常
     */
    @SuppressWarnings("unchecked")
    public <T extends ComponentDbData<I>> T addComponentDbData(final T dbData) {

        final Object idThat = dbData.getId();
        if (idThat != null) {
            throw new RuntimeException("the dbData has belong to an EntityDbData, this id:" + id +
                    ", that id:" + idThat + ", dbData:" + dbData);
        }

        dbData.setId(id);

        return (T) componentDbDataMap.put(dbData.getClass().getSimpleName(), dbData);
    }

    /**
     * 获取一个类型为T的组件
     * @param dbData 要获取的组件的{@link Class}
     * @param <T> 组件类型
     * @return 如果包含指定类型的组件，则返回该组件，否则返回null
     */
    @SuppressWarnings("unchecked")
    public <T extends ComponentDbData<I>> T getComponentDbData(final Class<T> dbData) {
        try {
            return (T) componentDbDataMap.get(dbData.getSimpleName());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取已被标脏的组件集合
     * @return 已被标脏的组件集合
     */
    public Set<ComponentDbData<I>> getDirtyComponentDbData() {
        return componentDbDataMap.values()
                .stream()
                .filter(DbData::isDirty)
                .collect(Collectors.toSet());
    }

    public <T extends ComponentDbData<I>> boolean hasComponentDbData(final Class<T> dbData) {
        return componentDbDataMap.containsKey(dbData.getSimpleName());
    }

    @Override
    public I getId() {
        return id;
    }

    @Override
    public void setId(I id) {
        this.id = id;
    }

    @Override
    public IUpdater getUpdater() {
        return updater;
    }

    @Override
    public void setUpdater(IUpdater update) {
        updater = update;
    }

    /**
     * 将所有的组件都更新
     */
    public void updateAllComponentDbData() {
        componentDbDataMap.forEach((key, componentDbData) -> updater.update(key, componentDbData));
    }

    /**
     * 更新某个组件
     * @param dbData 要更新的组件所属的{@link Class}
     * @param <T> 组件类型
     * @return 更新成功返回true，如果没有对应的组件，返回false
     */
    @SuppressWarnings("UnusedReturnValue")
    public <T extends ComponentDbData<I>> boolean updateComponentDbData(Class<T> dbData) {

        final String key = dbData.getSimpleName();
        final T componentDbData = getComponentDbData(dbData);
        if (componentDbData == null) {
            return false;
        }

        updater.update(key, componentDbData);
        return true;
    }
}



