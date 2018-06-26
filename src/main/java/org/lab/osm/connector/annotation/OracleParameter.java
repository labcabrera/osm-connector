package org.lab.osm.connector.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
// @Target(ElementType.ANNOTATION_TYPE)
public @interface OracleParameter {

	public enum ParameterType {
		IN, OUT
	}

	String name();

	int type();

	ParameterType mode();

	Class<?> returnStructClass() default UnmappedClass.class;

	static class UnmappedClass {

	}

}
