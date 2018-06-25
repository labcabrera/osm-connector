package org.lab.osm.connector;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

@Configuration
public class OsmConnectorConfiguration implements ImportAware {

	@Override
	public void setImportMetadata(AnnotationMetadata meta) {

	}

}
