package org.lab.osm.connector.exception;

import lombok.Getter;

/**
 * {@link OsmConnectorException} when invocation notations are not present in java classes.
 * 
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Getter
@SuppressWarnings("serial")
public class OsmMissingAnnotationException extends OsmConnectorException {

	private final Class<?> targetClasss;
	private final Class<?> annotationClass;

	/**
	 * Public constructor.
	 * 
	 * @param targetClass
	 * @param annotationClass
	 */
	public OsmMissingAnnotationException(Class<?> targetClass, Class<?> annotationClass) {
		super(String.format("Missing annotation %s in class %s", annotationClass.getName(), targetClass.getName()));
		this.targetClasss = targetClass;
		this.annotationClass = annotationClass;
	}

}
