package org.lab.soc.jdbc.metadata.model;

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

	public OracleMappingStructData() {
		fields = new ArrayList<>();
	}

	public void registerField(OracleMappingField field) {
		fields.add(field);
	}

}
