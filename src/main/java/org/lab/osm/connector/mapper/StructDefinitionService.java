package org.lab.osm.connector.mapper;

import java.sql.Connection;

import oracle.sql.ArrayDescriptor;
import oracle.sql.StructDescriptor;

/**
 * Service used to read Oracle descriptors from a given Oracle object names.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
public interface StructDefinitionService {

	/**
	 * Obtains the <code>StructDescriptor</code> from a given Oracle object name.
	 * 
	 * @param typeName
	 * @param conn
	 * @return
	 */
	StructDescriptor structDescriptor(String typeName, Connection conn);

	/**
	 * Obtains the <code>ArrayDescriptor</code> from a given Oracle collection name.
	 * 
	 * @param typeName
	 * @param conn
	 * @return
	 */
	ArrayDescriptor arrayDescriptor(String typeName, Connection conn);

}
