package org.lab.osm.connector.mapper;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.sql.STRUCT;

public interface StructMapper<T> {

	STRUCT toStruct(T source, Connection conn) throws SQLException;

	T fromStruct(STRUCT struct) throws SQLException;
}
