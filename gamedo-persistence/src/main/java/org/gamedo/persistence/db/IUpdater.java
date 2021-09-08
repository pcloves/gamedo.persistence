package org.gamedo.persistence.db;

import org.springframework.data.mongodb.core.query.UpdateDefinition;

/**
 * 一个包含前缀的增量更新器，该更新器负责对某一个文档（可能为内嵌文档）进行更新，假如有一个数据库，名为：player，其中一条文档记录为：
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
 * 且某{@link IUpdater}负责内嵌文档“ComponentDbPosition”的增量更新，那么：
 * <ul>
 * <li> {@link IUpdater#getPrefix()}返回值应该为：“ComponentDbPosition.”
 * <li> 可以调用{@link IUpdater#update(String, Object)}，对“x”和“y”进行更新，例如：updater.update("x", 10)
 * <li> 上一步执行完毕后，再将本{@link IUpdater}持久化到mongoDb前，调用{@link IUpdater#isDirty()}时，会返回true
 * </ul>
 */
public interface IUpdater extends UpdateDefinition {

    /**
     * 该更新器在mongoDB文档中的前缀，例如本更新器负责的数据库：player中，内嵌文档：ComponentDbPosition的增量更新，那么此处的前缀则为：
     * “ComponentDbPosition.”，如果本更新器负责的是整个文档的增量更新，则前缀为空字符串
     * @return 本更新器的前缀
     */
    String getPrefix();
    /**
     * 将所负责的文档（可能是内嵌文档）的字段进行更新
     * @param key 要更新的字段名
     * @param value the new value.
     */
    void update(String key, Object value);

    /**
     * 当前{@link IUpdater}是否已经更新过，返回true意味着更新方法：{@link IUpdater#update(String, Object)}被调用过；false意味着当前
     * 更新器的更新方法没有被调用
     * @return true意味着{@linkplain IUpdater#update(String, Object)}被调用过；false意味着当前更新器的更新方法没有被调用
     */
    boolean isDirty();
}
