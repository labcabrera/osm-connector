package org.lab.soc.jdbc;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.lab.soc.annotation.OracleField;
import org.lab.soc.annotation.OracleStruct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.jdbc.support.oracle.StructMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

/**
 * <code>StructMapper</code> implementation using custom annotations.
 *
 * @param <T> Mapped class.
 * 
 * @see org.lab.soc.annotation.OracleField
 */
@Slf4j
public class AnnotationStructMapper<T> implements StructMapper<T> {

	protected final StructDefinitionService definitionService;
	protected Class<T> mappedClass;
	protected Map<String, PropertyDescriptor> mappedFields;

	public AnnotationStructMapper(Class<T> mappedClass, StructDefinitionService definitionService) {
		this.definitionService = definitionService;
		this.initialize(mappedClass);
	}

	protected void initialize(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
		this.mappedFields = new HashMap<String, PropertyDescriptor>();
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
		try {
			for (int i = 0; i < pds.length; i++) {
				PropertyDescriptor pd = pds[i];
				if (pd.getWriteMethod() == null) {
					continue;
				}
				String name = pd.getName();
				Field declaredField = mappedClass.getDeclaredField(name);
				OracleField annotation = declaredField.getAnnotation(OracleField.class);
				if (annotation != null) {
					String oracleFieldValue = annotation.value();
					log.debug("Mapping {} to field {} using annotation name", oracleFieldValue, name);
					mappedFields.put(oracleFieldValue, pd);
				}
				else {
					log.debug("Mapping {} field using field name", name);
					mappedFields.put(name, pd);
				}
			}
		}
		catch (Exception ex) {
			throw new RuntimeException("Error mapping class " + mappedClass.getName(), ex);
		}
	}

	@Override
	public STRUCT toStruct(T source, Connection conn, String typeName) throws SQLException {
		StructDescriptor descriptor = definitionService.structDescriptor(typeName, conn);
		ResultSetMetaData rsmd = descriptor.getMetaData();
		int columns = rsmd.getColumnCount();
		Object[] values = new Object[columns];
		for (int i = 1; i <= columns; i++) {
			String column = JdbcUtils.lookupColumnName(rsmd, i).toLowerCase();
			PropertyDescriptor fieldMeta = (PropertyDescriptor) mappedFields.get(column);
			if (fieldMeta != null) {
				BeanWrapper bw = new BeanWrapperImpl(source);
				if (bw.isReadableProperty(fieldMeta.getName())) {
					try {
						if (log.isDebugEnabled()) {
							log.debug("Mapping column named \"" + column + "\"" + " to property \""
								+ fieldMeta.getName() + "\"");
						}
						values[i - 1] = bw.getPropertyValue(fieldMeta.getName());
					}
					catch (NotReadablePropertyException ex) {
						throw new DataRetrievalFailureException(
							"Unable to map column " + column + " to property " + fieldMeta.getName(), ex);
					}
				}
				else {
					log.warn("Unable to access the getter for " + fieldMeta.getName() + ".  Check that " + "get"
						+ StringUtils.capitalize(fieldMeta.getName()) + " is declared and has public access.");
				}
			}
		}
		// Modified from spring-data-oracle to recursive struct conversion
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) {
				continue;
			}
			Object obj = values[i];
			OracleStruct oracleAnnotation = obj.getClass().getAnnotation(OracleStruct.class);
			if (oracleAnnotation == null) {
				continue;
			}
			String typename = oracleAnnotation.value();
			log.info(String.format("Mapping %s to Oracle STRUCT %s", obj.getClass().getSimpleName(), typename));
			STRUCT tmp = toStruct(source, conn, typename);
			values[i] = tmp;
		}
		return new STRUCT(descriptor, conn, values);
	}

	public T fromStruct(STRUCT struct) throws SQLException {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		T mappedObject = BeanUtils.instantiateClass(this.mappedClass);
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
		ResultSetMetaData rsmd = struct.getDescriptor().getMetaData();
		Object[] attr = struct.getAttributes();
		int columnCount = rsmd.getColumnCount();
		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index).toLowerCase();
			PropertyDescriptor pd = (PropertyDescriptor) this.mappedFields.get(column);
			if (pd != null) {
				try {
					Object value = attr[index - 1];
					if (log.isDebugEnabled()) {
						log.debug("Mapping column '{}' to property '{}' of type {}", column, pd.getName(),
							pd.getPropertyType());
					}
					bw.setPropertyValue(pd.getName(), value);
				}
				catch (NotWritablePropertyException ex) {
					throw new DataRetrievalFailureException(
						"Unable to map column " + column + " to property " + pd.getName(), ex);
				}
			}
		}

		return mappedObject;
	}

}
