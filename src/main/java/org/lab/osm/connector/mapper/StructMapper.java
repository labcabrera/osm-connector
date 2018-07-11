package org.lab.osm.connector.mapper;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.sql.STRUCT;

/**
 * Component to perform the conversions between <code>oracle.sql.STRUCT</code> and our java model entities.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 * 
 * @param <T> Entity class
 */
public interface StructMapper<T> {

	/**
	 * Converts a model entity to an STRUCT object.
	 * 
	 * @param source
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	STRUCT toStruct(T source, Connection conn) throws SQLException;

	/**
	 * Converts a STRUCT objecto to a model entity.
	 * 
	 * @param source
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	T fromStruct(STRUCT struct) throws SQLException;
}
