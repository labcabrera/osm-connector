package org.lab.osm.connector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lab.cabrera@gmail.com
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OracleCollection {

	/**
	 * Oracle collection name.
	 * 
	 * @return
	 */
	String value() default StringUtils.EMPTY;

	/**
	 * Database owner.
	 * @return
	 */
	String owner() default StringUtils.EMPTY;

}
