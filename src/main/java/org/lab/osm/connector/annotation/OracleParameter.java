package org.lab.osm.connector.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.commons.lang3.StringUtils;

@Retention(RetentionPolicy.RUNTIME)
public @interface OracleParameter {

	public enum ParameterType {
		IN, OUT, IN_OUT
	}

	String name();

	String typeName() default StringUtils.EMPTY;

	int type();

	ParameterType mode();

	Class<?> returnStructClass() default UnmappedClass.class;

	static class UnmappedClass {

	}

}
