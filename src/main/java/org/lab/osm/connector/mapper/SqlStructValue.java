package org.lab.osm.connector.mapper;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.jdbc.core.support.AbstractSqlTypeValue;

import lombok.AllArgsConstructor;

/**
 * 
 * <code>AbstractSqlTypeValue</code> for Oracle STRUCT objects.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 *
 * @param <T>
 */
@AllArgsConstructor
public class SqlStructValue<T> extends AbstractSqlTypeValue {

	private final T source;

	private final StructMapper<T> mapper;

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.support.AbstractSqlTypeValue#createTypeValue(java.sql.Connection, int, java.lang.String)
	 */
	protected Object createTypeValue(Connection conn, int sqlType, String typeName) throws SQLException {
		return mapper.toStruct(this.source, conn);
	}

}
