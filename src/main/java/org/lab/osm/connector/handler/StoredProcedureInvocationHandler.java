package org.lab.osm.connector.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.lab.osm.connector.annotation.OracleParameter;
import org.lab.osm.connector.annotation.OracleStoredProcedure;
import org.lab.osm.connector.exception.OsmMissingAnnotationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
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
	private StoredProcedureHandlerParameterProcessor parameterProcessor;

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
					parameterProcessor.registerInputParameter(storedProcedure, parameter, inputMap, value);
					break;
				case OUT:
					parameterProcessor.registerOutputParameter(storedProcedure, parameter);
					break;
				default:
					value = inputArgs[inputMap.size()];
					parameterProcessor.registerInOutParameter(storedProcedure, parameter, inputMap, value);
					break;
				}
			}
		}
		storedProcedure.compile();
		long t0 = System.currentTimeMillis();
		Map<String, Object> result = storedProcedure.execute(inputMap);
		long t = System.currentTimeMillis() - t0;
		log.trace("Execution result ({}ms): {}", t, result);
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

}
