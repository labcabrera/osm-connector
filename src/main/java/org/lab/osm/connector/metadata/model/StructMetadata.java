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

	/**
	 * Java entity type.
	 */
	private Class<?> mappedClass;

	/**
	 * Oracle object name.
	 */
	private String strucyName;

	/**
	 * Mapped fields between Oracle and java type.
	 */
	private final List<FieldMetadata> fields;

	/**
	 * Unmapped fields (defined in java entity and not present in the oracle model).
	 */
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
