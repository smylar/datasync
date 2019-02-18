package com.sync.data;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * Specifies the tables an entity is associated with
 * 
 * @author paul.brandon
 *
 */
@Retention(RUNTIME)
public @interface Table {
    String schema();
    String name();
    String log();
    String group() default "";
    int groupOrder() default 0;
}
