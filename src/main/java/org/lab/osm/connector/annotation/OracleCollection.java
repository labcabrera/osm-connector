package org.lab.osm.connector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

/**
 * Sets the name of the Oracle collection into a field list:
 * 
 * <pre>
 * &#64;OracleCollection("MY_MODEL_CUSTOMERS")
 * private List<Customer> customers;
 * </pre>
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
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
