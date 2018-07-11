package org.lab.osm.connector.mapper.results;

import java.sql.CallableStatement;
import java.sql.SQLException;

import org.lab.osm.connector.mapper.ArrayMapper;
import org.springframework.jdbc.core.SqlReturnType;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oracle.sql.ARRAY;

/**
 * <code>SqlReturnType</code> implementation that returns a list of mapped entities.
 *
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 * 
 * @param <T> Domain entity type
 */
@AllArgsConstructor
@Slf4j
public class SqlListStructArray<T> implements SqlReturnType {

	private final ArrayMapper<T> mapper;

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.SqlReturnType#getTypeValue(java.sql.CallableStatement, int, int, java.lang.String)
	 */
	@Override
	public Object getTypeValue(CallableStatement cs, int i, int sqlType, String typeName) throws SQLException {
		log.trace("Binding Oracle ARRAY {} ({}) as a mapped entity list", typeName, sqlType);
		ARRAY array = (ARRAY) cs.getObject(i);
		if (array == null) {
			return null;
		}
		return mapper.fromArray(array);
	}

}