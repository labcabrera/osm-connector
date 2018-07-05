package org.lab.osm.connector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OracleStoredProcedure {

	String name();

	String owner() default "";

	String oraclePackage() default "";

	boolean isFunction() default false;

	OracleParameter[] parameters() default {};

}
