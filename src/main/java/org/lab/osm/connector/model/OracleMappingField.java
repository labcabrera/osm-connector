package org.lab.osm.connector.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OracleMappingField {

	private String javaAttributeName;

	private String oracleColumnName;
	private String oracleTypeName;
	private String oracleColumnClassName;
	private String oracleSchemaName;

	private Boolean mapped;
}
