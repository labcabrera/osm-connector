package org.lab.osm.connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
	String[] modelPackages() default {};

	/**
	 * List of packages used to discover proxy interface methods.
	 * @return Interface package list
	 */
	String[] repositoryPackages() default {};

	String dataBaseName() default "";

}
