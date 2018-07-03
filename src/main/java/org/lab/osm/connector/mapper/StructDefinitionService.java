package org.lab.osm.connector.mapper;

import java.sql.Connection;

import oracle.sql.ArrayDescriptor;
import oracle.sql.StructDescriptor;

public interface StructDefinitionService {

	StructDescriptor structDescriptor(String typeName, Connection conn);

	ArrayDescriptor arrayDescriptor(String typeName, Connection conn);

}
