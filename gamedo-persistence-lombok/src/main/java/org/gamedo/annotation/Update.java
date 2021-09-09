package org.gamedo.annotation;

import lombok.AccessLevel;
import org.gamedo.persistence.db.DbData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解到{@link DbData}子类或其字段上, gamedo-persistence-lombok会自动生成<b>update</b>方法，例如：<p>
 * <pre>
 * &#64;MarkDirty private int x;
 * </pre>
 *
 * 将自动生成：<p>
 *
 * <pre>
 * public void updateX() {
 *      this.update("x", this.x);
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface Update {

    /**
     * 生成的<b>update</b>方法的访问等级
     * @return 访问等级
     */
    AccessLevel value() default lombok.AccessLevel.PUBLIC;

    /**
     * 注解在类的字段上，意味着该字段的<b>update</b>方法不会自动生成
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Exclude
    {
    }
}
