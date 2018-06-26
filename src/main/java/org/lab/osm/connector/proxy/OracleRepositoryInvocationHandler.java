package org.lab.osm.connector.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.lab.osm.connector.annotation.OracleParameter;
import org.lab.osm.connector.annotation.OracleStoredProcedure;
import org.lab.osm.connector.service.StructMapperService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.support.oracle.SqlReturnStruct;
import org.springframework.data.jdbc.support.oracle.SqlStructValue;
import org.springframework.data.jdbc.support.oracle.StructMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OracleRepositoryInvocationHandler<T> implements FactoryBean<T>, InvocationHandler {

	@Autowired
	private DataSource dataSource;
	@Autowired
	private StructMapperService mapperService;

	private final Class<T> interfaceClass;
	private final ClassLoader classLoader;

	public OracleRepositoryInvocationHandler(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		log.debug("Invoking proxy using {}", interfaceClass.getName());

		OracleStoredProcedure annotation = interfaceClass.getAnnotation(OracleStoredProcedure.class);
		Assert.notNull(annotation,
			"Missing required OracleStoredProcedure annotation on interface " + interfaceClass.getName());

		String storedProcedureName = annotation.name();

		StoredProcedure storedProcedure = new DelegateStoredProcedure(dataSource, storedProcedureName);
		storedProcedure.setFunction(annotation.isFunction());

		Map inputMap = new HashMap();

		for (OracleParameter parameter : annotation.parameters()) {
			String name = parameter.name();
			int type = parameter.type();
			switch (parameter.mode()) {
			case IN:
				SqlParameter sqlParam = new SqlParameter(name, type);
				storedProcedure.declareParameter(sqlParam);

				// TODO check struct value
				Object value = args[inputMap.size()];
				StructMapper structMapper = mapperService.mapper(value.getClass());
				inputMap.put(name, new SqlStructValue(value, structMapper));

				break;
			case OUT:
				SqlReturnStruct sqlReturn = new SqlReturnStruct(parameter.returnStructClass());
				storedProcedure.declareParameter(new SqlOutParameter(name, type, name, sqlReturn));
				break;
			}
		}
		storedProcedure.compile();

		// Collections.singletonMap("P_O_SIN_ACC_IN_S", new SqlStructValue(request, requestMapper));

		Map<String, Object> result = storedProcedure.execute(inputMap);

		log.trace("Execution result: {}", result);

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		return (T) Proxy.newProxyInstance(classLoader, new Class<?>[] { interfaceClass }, this);
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

}
