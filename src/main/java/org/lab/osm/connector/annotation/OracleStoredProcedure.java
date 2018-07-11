package org.lab.osm.connector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 *
 * @see org.lab.osm.connector.handler.StoredProcedureExecutor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OracleStoredProcedure {

	String name();

	String owner() default StringUtils.EMPTY;

	String oraclePackage() default StringUtils.EMPTY;

	boolean isFunction() default false;

	OracleParameter[] parameters() default {};

}
