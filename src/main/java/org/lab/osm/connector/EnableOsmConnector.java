package org.lab.osm.connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(OsmConnectorConfiguration.class)
public @interface EnableOsmConnector {

	String[] modelPackages() default {};

	String[] procedurePackages() default {};

}
