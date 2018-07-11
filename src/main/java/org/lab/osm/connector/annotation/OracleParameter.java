package org.lab.osm.connector.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.commons.lang3.StringUtils;

@Retention(RetentionPolicy.RUNTIME)
public @interface OracleParameter {

	public enum ParameterType {
		IN, OUT, IN_OUT
	}

	/**
	 * Oracle parameter name.
	 * @return
	 */
	String name();

	/**
	 * Oracle type name.
	 * @return
	 */
	String typeName() default StringUtils.EMPTY;

	/**
	 * Jdbc SQL type.
	 * @return
	 */
	int type();

	/**
	 * Parameter mode.
	 * @return
	 */
	ParameterType mode();

	/**
	 * Entity class to map execution results.
	 * @return
	 */
	Class<?> returnStructClass() default UnmappedClass.class;

	/**
	 * Default <code>OracleParameter.returnStructClass</code>.
	 * 
	 * @author lab.cabrera@gmail.com
	 * @since 1.0.0
	 *
	 */
	static class UnmappedClass {
	}

}
