package org.lab.osm.connector.metadata;

import org.lab.osm.connector.metadata.model.MappingMetadata;

/**
 * Component used to populate {@link MappingMetadata} information.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
public interface MetadataCollector {

	/**
	 * Updates the {@link MappingMetadata} with all Oracle entities found in a given package.
	 * 
	 * @param metadata
	 * @param packageName
	 */
	void readMetadata(MappingMetadata metadata, String packageName);

}
