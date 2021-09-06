package org.gamedo.persistence.annotations;

import org.gamedo.persistence.db.EntityDbData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * indicate a field of {@linkplain } is a map contains {@linkplain }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityDbDataComponent {

    Class<? extends EntityDbData> value();
}
