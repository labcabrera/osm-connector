package org.lab.osm.connector.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.NotImplementedException;
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
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnType;
import org.springframework.jdbc.object.StoredProcedure;

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
public class OracleRepositoryInvocationHandler<T> implements FactoryBean<T>, InvocationHandler {

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
	public OracleRepositoryInvocationHandler(Class<T> interfaceClass) {
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

		String storedProcedureName = annotation.name();
		log.trace("Using stored procedure {}", storedProcedureName);

		StoredProcedure storedProcedure = new DelegateStoredProcedure(dataSource, storedProcedureName);
		storedProcedure.setFunction(annotation.isFunction());

		Map inputMap = new HashMap();
		// TODO revisar como llegan los parametros
		Object[] inputArgs = (Object[]) args[0];

		for (OracleParameter parameter : annotation.parameters()) {
			switch (parameter.mode()) {
			case IN:
				Object value = inputArgs[inputMap.size()];
				registerInputParameter(storedProcedure, parameter, inputMap, value);
				break;
			case OUT:
				registerOutputParameter(storedProcedure, parameter);
				break;
			default:
				// TODO in-out parameter
				throw new NotImplementedException("Unsupported parameter mode " + parameter.mode());
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerInputParameter(StoredProcedure storedProcedure, OracleParameter parameter,
		Map<String, Object> inputMap, Object value) {
		String name = parameter.name();
		int type = parameter.type();

		SqlParameter sqlParam = new SqlParameter(name, type);
		storedProcedure.declareParameter(sqlParam);

		switch (type) {
		case Types.STRUCT:
			StructMapper<?> structMapper = mapperService.mapper(value.getClass());
			inputMap.put(name, new SqlStructValue(value, structMapper));
			break;
		default:
			// TODO
			throw new NotImplementedException("Usupported type " + type);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerOutputParameter(StoredProcedure storedProcedure, OracleParameter parameter) {
		String name = parameter.name();
		int type = parameter.type();
		Class<?> returnClass = parameter.returnStructClass();
		StructMapper<?> structMapper;
		SqlReturnType sqlReturn;

		switch (parameter.type()) {
		case Types.STRUCT:
			structMapper = mapperService.mapper(returnClass);
			sqlReturn = new SqlReturnStruct(structMapper);
			storedProcedure.declareParameter(new SqlOutParameter(name, type, name, sqlReturn));
			break;
		case Types.ARRAY:
			if (returnClass != null) {
				structMapper = mapperService.mapper(returnClass);
				sqlReturn = new SqlListStructArray(structMapper);
				storedProcedure.declareParameter(new SqlOutParameter(name, Types.ARRAY, name, sqlReturn));
			}
			else {
				// TODO
				throw new NotImplementedException("Not implemented primitive array return type");
			}
			break;
		default:
			// TODO
			throw new NotImplementedException("Unsupported output type " + parameter.type());
		}

	}
}
