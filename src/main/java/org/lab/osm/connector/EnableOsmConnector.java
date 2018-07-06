package org.lab.osm.connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Import;

/**
 * Annotation configuration for {@link OsmConnectorConfiguration}.
 *
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(OsmConnectorConfiguration.class)
public @interface EnableOsmConnector {

	/**
	 * List of packages used to scan entity model objects.
	 * @return Model package list.
	 */
	String[] modelPackages();

	/**
	 * List of packages used to discover proxy interface methods.
	 * @return Interface package list
	 */
	String[] executorPackages();

	String serializationFolder() default StringUtils.EMPTY;

	String dataBaseName() default StringUtils.EMPTY;

}
