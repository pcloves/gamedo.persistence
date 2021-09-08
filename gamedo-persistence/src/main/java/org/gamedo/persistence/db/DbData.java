package org.gamedo.persistence.db;

/**
 * {@link DbData}接口，意味着：
 * <ul>
 * <li>对应一个唯一id，执行更新操作</li>
 * <li>包含一个{@link IUpdater}，从而实现增量更新</li>
 * </ul>
 */
public interface DbData {
    /**
     * @return 返回唯一id，该id映射在mongoDB的_id字段上
     */
    String getId();

    /**
     * @param id 设置唯一id
     */
    void setId(String id);

    /**
     * 获取当前正在使用的增量更新器
     * @return 返回正在使用的增量更新器
     */
    IUpdater getUpdater();

    /**
     * 设置一个新的增量更新器，{@link DbData}的增量更新器都是一次性，意味着每一次存盘后，都会设置一个新的增量更新器
     * @param updater 新设置的增量更新器
     */
    void setUpdater(IUpdater updater);

    /**
     * 更新一个字段：key的值为value，实现类需要将value序列化为mongoDB原生的存储数据
     * @param key 要更新的字段名.
     * @param value 要更新的数值.
     */
    default void update(String key, Object value) {
        getUpdater().update(key, value);
    }

    /**
     * 当前{@link DbData}是否已经更新过，返回true意味着更新方法：{@link DbData#update(String, Object)}被调用过，且没有持久化到db中；
     * false意味着当前更新器的更新方法没有被调用
     * @return true意味着{@linkplain IUpdater#update(String, Object)}被调用过，且没有持久化到db中；false意味着当前更新器的更新方法没
     * 有被调用
     */
    default boolean isDirty() {
        return getUpdater().isDirty();
    }
}
