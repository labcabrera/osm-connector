package org.lab.osm.connector.mapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import oracle.sql.ARRAY;

public interface ArrayMapper<T> {

	ARRAY toArray(List<T> source, Connection connection) throws SQLException;

	List<T> fromArray(ARRAY array) throws SQLException;
}
