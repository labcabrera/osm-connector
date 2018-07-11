package org.lab.osm.connector.handler;

import java.sql.JDBCType;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.lab.osm.connector.annotation.OracleParameter;
import org.lab.osm.connector.mapper.ArrayMapper;
import org.lab.osm.connector.mapper.SqlArrayValue;
import org.lab.osm.connector.mapper.SqlStructValue;
import org.lab.osm.connector.mapper.StructMapper;
import org.lab.osm.connector.mapper.results.SqlListStructArray;
import org.lab.osm.connector.mapper.results.SqlReturnStruct;
import org.lab.osm.connector.service.StructMapperService;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnType;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.util.Assert;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Slf4j
public class StoredProcedureHandlerParameterProcessor {

	private final StructMapperService mapperService;

	public StoredProcedureHandlerParameterProcessor(@NonNull StructMapperService mapperService) {
		this.mapperService = mapperService;
	}

	public void registerInputParameter(StoredProcedure storedProcedure, OracleParameter parameter,
		Map<String, Object> inputMap, Object value) {
		int type = parameter.type();
		String name = parameter.name();
		String typeName = StringUtils.isNotBlank(parameter.typeName()) ? parameter.typeName() : valueOfType(type);
		log.trace("Register input parameter '{}' ({}/{}): '{}'", name, typeName, type, value);
		SqlParameter sqlParam = new SqlParameter(name, type, parameter.typeName());
		storedProcedure.declareParameter(sqlParam);
		addToInputMap(parameter, inputMap, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerOutputParameter(StoredProcedure storedProcedure, OracleParameter parameter) {
		String name = parameter.name();
		String typeName = parameter.typeName();
		int type = parameter.type();
		Class<?> returnClass = parameter.returnStructClass();
		StructMapper<?> structMapper;
		SqlReturnType sqlReturn;

		switch (parameter.type()) {
		case Types.STRUCT:
			structMapper = mapperService.mapper(returnClass);
			sqlReturn = new SqlReturnStruct(structMapper);
			log.trace("Register output struct parameter '{}' using type '{}'", name, typeName);
			storedProcedure.declareParameter(new SqlOutParameter(name, type, typeName, sqlReturn));
			break;
		case Types.ARRAY:
			if (returnClass != null) {
				structMapper = mapperService.mapper(returnClass);
				sqlReturn = new SqlListStructArray(structMapper);
				log.trace("Register output array parameter '{}' using type '{}'", name, typeName);
				storedProcedure.declareParameter(new SqlOutParameter(name, Types.ARRAY, typeName, sqlReturn));
			}
			else {
				// TODO
				throw new NotImplementedException("Not implemented primitive array return type");
			}
			break;
		case Types.NVARCHAR:
		case Types.NUMERIC:
		case Types.DATE:
			log.trace("Register output primitive parameter '{}' as '{}'", name, valueOfType(type));
			storedProcedure.declareParameter(new SqlOutParameter(name, type));
			break;
		default:
			// TODO
			throw new NotImplementedException("Unsupported output type " + parameter.type());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerInOutParameter(StoredProcedure storedProcedure, OracleParameter parameter,
		Map<String, Object> inputMap, Object value) {
		String name = parameter.name();
		String typeName = parameter.typeName();
		int type = parameter.type();
		Class<?> returnClass = parameter.returnStructClass();
		StructMapper<?> structMapper;
		SqlReturnType sqlReturn;
		switch (parameter.type()) {
		case Types.STRUCT:
			structMapper = mapperService.mapper(returnClass);
			sqlReturn = new SqlReturnStruct(structMapper);
			log.trace("Register in-out struct parameter '{}' using type '{}'", name, typeName);
			storedProcedure.declareParameter(new SqlInOutParameter(name, type, typeName, sqlReturn));
			break;
		case Types.ARRAY:
			if (returnClass != null) {
				structMapper = mapperService.mapper(returnClass);
				sqlReturn = new SqlListStructArray(structMapper);
				log.trace("Register in-out array parameter '{}' using type '{}'", name, typeName);
				storedProcedure.declareParameter(new SqlInOutParameter(name, Types.ARRAY, typeName, sqlReturn));
			}
			else {
				// TODO
				throw new NotImplementedException("Not implemented primitive array return type");
			}
			break;
		case Types.NVARCHAR:
		case Types.NUMERIC:
		case Types.DATE:
			log.trace("Register in-out primitive parameter '{}' as '{}'", name, valueOfType(type));
			storedProcedure.declareParameter(new SqlInOutParameter(name, type));
			break;
		default:
			// TODO
			throw new NotImplementedException("Unsupported output type " + parameter.type());
		}
		addToInputMap(parameter, inputMap, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addToInputMap(OracleParameter parameter, Map<String, Object> inputMap, Object value) {
		if (value == null) {
			inputMap.put(parameter.name(), null);
			return;
		}
		switch (parameter.type()) {
		case Types.STRUCT:
			// Struct conversion
			StructMapper<?> structMapper = mapperService.mapper(value.getClass());
			inputMap.put(parameter.name(), new SqlStructValue(value, structMapper));
			break;
		case Types.ARRAY:
			// Array conversion
			Assert.isInstanceOf(List.class, value);
			String oracleCollectionName = parameter.typeName();
			List list = (List) value;

			Object firstNotNull = list.stream().filter(x -> x != null).findFirst().orElseGet(null);
			Class<?> mappedClass = firstNotNull.getClass();

			ArrayMapper arrayMapper = mapperService.arrayMapper(mappedClass, oracleCollectionName);
			inputMap.put(parameter.name(), new SqlArrayValue<>(list, arrayMapper));
			break;
		case Types.VARCHAR:
		case Types.NVARCHAR:
		case Types.NUMERIC:
			// Direct value
			inputMap.put(parameter.name(), value);
			break;
		case Types.DATE:
			// Java sql date conversion
			Date valueAsDate = (Date) value;
			java.sql.Date sqlDate = valueAsDate != null ? new java.sql.Date(valueAsDate.getTime()) : null;
			inputMap.put(parameter.name(), sqlDate);
			break;
		default:
			// TODO
			throw new NotImplementedException("Usupported input parameter conversion type " + parameter.type());
		}
	}

	private String valueOfType(int sqlType) {
		return JDBCType.valueOf(sqlType).getName();
	}
}
