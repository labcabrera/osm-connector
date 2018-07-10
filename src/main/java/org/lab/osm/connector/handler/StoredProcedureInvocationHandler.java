package org.lab.osm.connector.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.JDBCType;
import java.sql.Types;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.lab.osm.connector.annotation.OracleParameter;
import org.lab.osm.connector.annotation.OracleStoredProcedure;
import org.lab.osm.connector.exception.OsmMissingAnnotationException;
import org.lab.osm.connector.mapper.SqlStructValue;
import org.lab.osm.connector.mapper.StructMapper;
import org.lab.osm.connector.mapper.results.SqlListStructArray;
import org.lab.osm.connector.mapper.results.SqlReturnStruct;
import org.lab.osm.connector.service.StructMapperService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnType;
import org.springframework.jdbc.object.StoredProcedure;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <code>InvocationHandler</code> to invoke <code>StoredProcedure</code> based on {@link OracleStoredProcedure}
 * annotation.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 * 
 * @param <T> Interface class.
 */
@Slf4j
public class StoredProcedureInvocationHandler<T> implements FactoryBean<T>, InvocationHandler {

	@Autowired
	private DataSource dataSource;
	@Autowired
	private StructMapperService mapperService;

	private final Class<T> interfaceClass;
	private final ClassLoader classLoader;

	/**
	 * Public constructor from service interface class.
	 * 
	 * @param interfaceClass
	 */
	public StoredProcedureInvocationHandler(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		log.debug("Invoking stored procedure handler using interface {}", interfaceClass.getName());

		OracleStoredProcedure annotation = interfaceClass.getAnnotation(OracleStoredProcedure.class);
		if (annotation == null) {
			throw new OsmMissingAnnotationException(interfaceClass, OracleStoredProcedure.class);
		}

		String storedProcedureName = resolveStoredProcedureName(annotation);
		log.trace("Using stored procedure {}", storedProcedureName);

		StoredProcedure storedProcedure = new DelegateStoredProcedure(dataSource, storedProcedureName);
		storedProcedure.setFunction(annotation.isFunction());

		Map inputMap = new LinkedHashMap();
		if (args != null && args.length > 0 && ((Object[]) args[0]).length > 0) {
			Object[] inputArgs = (Object[]) args[0];
			Object value = null;
			for (OracleParameter parameter : annotation.parameters()) {
				switch (parameter.mode()) {
				case IN:
					value = inputArgs[inputMap.size()];
					registerInputParameter(storedProcedure, parameter, inputMap, value);
					break;
				case OUT:
					registerOutputParameter(storedProcedure, parameter);
					break;
				default:
					value = inputArgs[inputMap.size()];
					registerInOutParameter(storedProcedure, parameter, inputMap, value);
					break;
				}
			}
		}
		storedProcedure.compile();
		Map<String, Object> result = storedProcedure.execute(inputMap);
		log.trace("Execution result: {}", result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		return (T) Proxy.newProxyInstance(classLoader, new Class<?>[] { interfaceClass }, this);
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

	private void registerInputParameter(StoredProcedure storedProcedure, OracleParameter parameter,
		Map<String, Object> inputMap, Object value) {
		int type = parameter.type();
		String name = parameter.name();
		String typeName = StringUtils.isNotBlank(parameter.typeName()) ? parameter.typeName() : valueOfType(type);
		log.trace("Register input parameter '{}' ({}/{}): '{}'", name, typeName, type, value);
		SqlParameter sqlParam = new SqlParameter(name, type, parameter.typeName());
		storedProcedure.declareParameter(sqlParam);
		addToInputMap(parameter.name(), parameter.type(), inputMap, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerOutputParameter(StoredProcedure storedProcedure, OracleParameter parameter) {
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
	private void registerInOutParameter(StoredProcedure storedProcedure, OracleParameter parameter,
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
		addToInputMap(parameter.name(), parameter.type(), inputMap, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addToInputMap(String parameterName, int sqlType, Map<String, Object> inputMap, Object value) {
		switch (sqlType) {
		case Types.STRUCT:
			// Struct conversion
			StructMapper<?> structMapper = mapperService.mapper(value.getClass());
			inputMap.put(parameterName, new SqlStructValue(value, structMapper));
			break;
		case Types.VARCHAR:
		case Types.NVARCHAR:
		case Types.NUMERIC:
			// Direct value
			inputMap.put(parameterName, value);
			break;
		case Types.DATE:
			// Java sql date conversion
			Date valueAsDate = (Date) value;
			java.sql.Date sqlDate = valueAsDate != null ? new java.sql.Date(valueAsDate.getTime()) : null;
			inputMap.put(parameterName, sqlDate);
			break;
		default:
			// TODO
			throw new NotImplementedException("Usupported type " + sqlType);
		}
	}

	private String resolveStoredProcedureName(@NonNull OracleStoredProcedure annotation) {
		String owner = annotation.owner();
		String oraclePackage = annotation.oraclePackage();
		String name = annotation.name();
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(owner)) {
			sb.append(owner).append(".");
		}
		if (StringUtils.isNotBlank(oraclePackage)) {
			sb.append(oraclePackage).append(".");
		}
		sb.append(name);
		return sb.toString();
	}

	private String valueOfType(int sqlType) {
		return JDBCType.valueOf(sqlType).getName();
	}
}
