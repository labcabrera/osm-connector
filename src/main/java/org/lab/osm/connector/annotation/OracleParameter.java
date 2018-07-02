package org.lab.osm.connector.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.commons.lang3.StringUtils;

@Retention(RetentionPolicy.RUNTIME)
// @Target(ElementType.ANNOTATION_TYPE)
public @interface OracleParameter {

	public enum ParameterType {
		IN, OUT
	}

	String name();

	String typeName() default StringUtils.EMPTY;

	int type();

	ParameterType mode();

	Class<?> returnStructClass() default UnmappedClass.class;

	static class UnmappedClass {

	}

}
