package org.lab.osm.connector.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleMappingStructData {

	private Class<?> mappedClass;
	private String strucyName;
	private int attributeCount;
	private final List<OracleMappingField> fields;
	// TODO change bean def
	private final List<OracleMappingField> unmappedFields;

	public OracleMappingStructData() {
		fields = new ArrayList<>();
		unmappedFields = new ArrayList<>();
	}

	public void registerField(OracleMappingField field) {
		fields.add(field);
	}

	public void registerUnmappedField(OracleMappingField field) {
		unmappedFields.add(field);
	}

}
