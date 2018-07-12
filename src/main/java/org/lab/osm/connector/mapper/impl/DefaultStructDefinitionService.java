package org.lab.osm.connector.mapper.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.lab.osm.connector.exception.OsmConnectorException;
import org.lab.osm.connector.mapper.StructDefinitionService;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import oracle.sql.ArrayDescriptor;
import oracle.sql.StructDescriptor;

/**
 * 
 * Default {@link StructDefinitionService} storing in memory Oracle STRUCT mappings.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Slf4j
public class DefaultStructDefinitionService implements StructDefinitionService {

	private final Map<String, StructDescriptor> structDescriptorValues;
	private final Map<String, ArrayDescriptor> arrayDescriptorValues;

	/**
	 * Public constructor.
	 */
	public DefaultStructDefinitionService() {
		structDescriptorValues = new HashMap<>();
		arrayDescriptorValues = new HashMap<>();
	}

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.mapper.StructDefinitionService#structDescriptor(java.lang.String, java.sql.Connection)
	 */
	@Override
	public StructDescriptor structDescriptor(@NonNull String typeName, Connection connection) {
		try {
			if (structDescriptorValues.containsKey(typeName)) {
				return structDescriptorValues.get(typeName);
			}
			else {
				log.info("Reading struct {} descriptor from database", typeName);
				StructDescriptor desc = StructDescriptor.createDescriptor(typeName, connection);
				structDescriptorValues.put(typeName, desc);
				return desc;
			}
		}
		catch (Exception ex) {
			throw new OsmConnectorException("Error reading struct descriptor " + typeName, ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.mapper.StructDefinitionService#arrayDescriptor(java.lang.String, java.sql.Connection)
	 */
	@Override
	public ArrayDescriptor arrayDescriptor(@NonNull String typeName, Connection connection) {
		try {
			if (arrayDescriptorValues.containsKey(typeName)) {
				return arrayDescriptorValues.get(typeName);
			}
			else {
				log.info("Reading array {} descriptor from database", typeName);
				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(typeName, connection);
				arrayDescriptorValues.put(typeName, desc);
				return desc;
			}
		}
		catch (Exception ex) {
			throw new OsmConnectorException("Error reading array descriptor " + typeName, ex);
		}
	}

}
