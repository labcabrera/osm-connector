package org.lab.osm.connector.metadata;

import org.lab.osm.connector.metadata.model.MappingMetadata;

public interface MetadataCollector {

	void readMetadata(MappingMetadata metadata, String packageName);

}
