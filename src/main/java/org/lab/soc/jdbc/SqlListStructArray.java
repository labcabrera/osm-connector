package org.lab.soc.jdbc;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jdbc.support.oracle.SqlReturnArray;
import org.springframework.data.jdbc.support.oracle.StructMapper;

import lombok.AllArgsConstructor;
import oracle.sql.ARRAY;
import oracle.sql.STRUCT;

/**
 * <code>SqlReturnArray</code> implementation that returns a list of domain entities.
 *
 * @param <T> Domain entity type
 */
@AllArgsConstructor
public class SqlListStructArray<T> extends SqlReturnArray {

	private final StructMapper<T> mapper;

	@Override
	public Object getTypeValue(CallableStatement cs, int i, int sqlType, String typeName) throws SQLException {
		ARRAY array = (ARRAY) cs.getObject(i);
		if (array == null) {
			return null;
		}
		Object[] values = (Object[]) array.getArray();
		List<T> list = new ArrayList<>();
		for (int z = 0; z < values.length; z++) {
			STRUCT struct = (STRUCT) values[z];
			T p = (T) mapper.fromStruct(struct);
			list.add(p);
		}
		return list;
	}

}