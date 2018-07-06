package org.lab.osm.connector.metadata.model;

import java.util.ArrayList;
import java.util.List;

import org.lab.osm.connector.exception.OsmMappingException;

import lombok.Getter;
import lombok.NonNull;

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

	public void register(@NonNull StructMetadata data) {
		if (isDefinedStruct(data.getStrucyName())) {
			throw new OsmMappingException("Struct name " + data.getStrucyName() + " is already defined");
		}
		else if (isDefinedStruct(data.getMappedClass())) {
			throw new OsmMappingException("Struct class " + data.getClass().getName() + " is already defined");
		}
		structs.add(data);
	}

	public void registerPackageName(@NonNull String packageName) {
		packageNames.add(packageName);
	}

	public boolean isDefinedStruct(@NonNull String structName) {
		return structs.stream().filter(x -> structName.equals(x.getStrucyName())).count() > 0;
	}

	public boolean isDefinedStruct(@NonNull Class<?> javaType) {
		return structs.stream().filter(x -> javaType.equals(x.getMappedClass())).count() > 0;
	}

}
