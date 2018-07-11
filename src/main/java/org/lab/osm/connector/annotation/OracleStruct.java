package org.lab.osm.connector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

/**
 * Annotation used to resolve Oracle name of a given java entity.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OracleStruct {

	/**
	 * Oracle entity name.
	 * 
	 * @return
	 */
	String value() default StringUtils.EMPTY;

}
