package org.lab.osm.connector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OracleField {

	/**
	 * Oracle entity name.
	 * 
	 * @return
	 */
	String value() default "";

	String typeName() default "";

	int length() default -1;

	int precision() default -1;

	int scale() default -1;

}
