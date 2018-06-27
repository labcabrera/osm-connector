package org.lab.osm.connector.metadata.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Entity to store all the information of the mapped objects of our Oracle model.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Getter
public class MappingMetadata {

	private final List<StructMetadata> structs;
	private final List<String> packageNames;

	/**
	 * Public constructor.
	 */
	public MappingMetadata() {
		structs = new ArrayList<>();
		packageNames = new ArrayList<>();
	}

	public void register(StructMetadata data) {
		structs.add(data);
	}

	public void registerPackageName(String packageName) {
		packageNames.add(packageName);
	}

}
