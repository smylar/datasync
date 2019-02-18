package com.sync.data;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies field column name in the database
 * 
 * @author paul.brandon
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Column {
    String name();
}
