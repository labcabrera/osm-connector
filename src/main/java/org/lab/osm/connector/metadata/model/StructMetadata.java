package org.lab.osm.connector.metadata.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the metadata of an Oracle data structure.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Getter
@Setter
public class StructMetadata {

	private Class<?> mappedClass;
	private String strucyName;
	private int attributeCount;
	private final List<FieldMetadata> fields;
	private final List<FieldMetadata> unmappedFields;

	/**
	 * Public constructor.
	 */
	public StructMetadata() {
		fields = new ArrayList<>();
		unmappedFields = new ArrayList<>();
	}

	public void registerField(FieldMetadata field) {
		fields.add(field);
	}

	public void registerUnmappedField(FieldMetadata field) {
		unmappedFields.add(field);
	}

}
