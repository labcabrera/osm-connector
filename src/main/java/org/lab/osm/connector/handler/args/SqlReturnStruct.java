package org.lab.osm.connector.handler.args;

import java.sql.CallableStatement;
import java.sql.SQLException;

import org.lab.osm.connector.mapper.StructMapper;
import org.springframework.jdbc.core.SqlReturnType;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oracle.sql.STRUCT;

/**
 * <code>SqlReturnType</code> implementation that returns a mapped entity representation of a Oracle STRUCT result.
 *
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 * 
 * @param <T> Domain entity type
 */
@Slf4j
@AllArgsConstructor
public class SqlReturnStruct implements SqlReturnType {

	private final StructMapper<?> mapper;

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.SqlReturnType#getTypeValue(java.sql.CallableStatement, int, int, java.lang.String)
	 */
	@Override
	public Object getTypeValue(CallableStatement cs, int paramIndex, int sqlType, String typeName) throws SQLException {
		log.trace("Binding Oracle STRUCT {} ({}) as mapped entity", typeName, sqlType);
		STRUCT struct = (STRUCT) cs.getObject(paramIndex);
		if (struct == null) {
			return null;
		}
		return mapper.fromStruct(struct);
	}

}
