package org.lab.osm.connector.mapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import oracle.sql.ARRAY;

/**
 * Component to perform the conversions between <code>oracle.sql.ARRAY</code> and lists of java model entities.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 * 
 * @param <T> Entity class
 * 
 * @see StructMapper
 */
public interface ArrayMapper<T> {

	ARRAY toArray(List<T> source, Connection connection) throws SQLException;

	List<T> fromArray(ARRAY array) throws SQLException;
}
