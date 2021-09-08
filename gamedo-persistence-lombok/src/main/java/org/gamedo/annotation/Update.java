package org.gamedo.annotation;

import lombok.AccessLevel;
import org.gamedo.persistence.db.DbData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotated on a {@link DbData} sub class or it's class fields, gamedo-persistence-lombok will generate <b>markDirty</b>
 * method, for example:<p>
 * <pre>
 * &#64;MarkDirty private int x;
 * </pre>
 *
 * will generate:<p>
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
     * access level of the <b>markDirty</b> method
     * @return the access level
     */
    AccessLevel value() default lombok.AccessLevel.PUBLIC;

    /**
     * annotate on a field, meaning that the <b>markDirty</b> method won't generated.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Exclude
    {
    }
}
