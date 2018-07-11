package org.lab.osm.connector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

/**
 * Sets the name of a property of an Oracle object.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OracleField {

	/**
	 * Oracle entity name.
	 * 
	 * @return
	 */
	String value() default StringUtils.EMPTY;

	/**
	 * Oracle type name.
	 * @return
	 */
	String typeName() default StringUtils.EMPTY;

	/**
	 * Field length.
	 * @return
	 */
	int length() default -1;

	/**
	 * Field precision.
	 * @return
	 */
	int precision() default -1;

	/**
	 * Field scale.
	 * @return
	 */
	int scale() default -1;

}
