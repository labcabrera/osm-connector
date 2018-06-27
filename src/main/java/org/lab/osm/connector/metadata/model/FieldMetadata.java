package org.lab.osm.connector.metadata.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents the fields required to map a field within an Oracle STRUCT.
 *
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
public class FieldMetadata {

	private String javaAttributeName;

	private String oracleColumnName;
	private String oracleTypeName;
	private String oracleColumnClassName;
	private String oracleSchemaName;

	private boolean mapped;
}
