package org.lab.osm.connector.exception;

import lombok.Getter;

@Getter
@SuppressWarnings("serial")
public class OsmMissingAnnotationException extends OsmConnectorException {

	private final Class<?> targetClasss;
	private final Class<?> annotationClass;

	public OsmMissingAnnotationException(Class<?> targetClass, Class<?> annotationClass) {
		super(String.format("Missing annotation %s in class %s", annotationClass.getName(), targetClass.getName()));
		this.targetClasss = targetClass;
		this.annotationClass = annotationClass;
	}

}
