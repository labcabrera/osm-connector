package org.lab.osm.connector.mapper.impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;
import org.lab.osm.connector.annotation.OracleCollection;
import org.lab.osm.connector.exception.OsmMappingException;
import org.lab.osm.connector.mapper.ArrayMapper;
import org.lab.osm.connector.mapper.StructDefinitionService;
import org.lab.osm.connector.mapper.StructMapper;
import org.lab.osm.connector.mapper.StructMapperService;
import org.lab.osm.connector.metadata.model.FieldMetadata;
import org.lab.osm.connector.metadata.model.MappingMetadata;
import org.lab.osm.connector.metadata.model.StructMetadata;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleArray;
import oracle.sql.ARRAY;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

/**
 * 
 * Default {@link StructMapper} using metadata information from the entity model annotations.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Slf4j
public class MetadataStructMapper<T> implements StructMapper<T> {

	private final Class<T> mappedClass;
	private final StructMapperService mapperService;
	private final MappingMetadata metadata;
	private final StructDefinitionService definitionService;

	// TODO consider using service
	private final UnaryOperator<String> nameNormalizer;

	public MetadataStructMapper( //@formatter:off
			Class<T> mappingClass,
			StructMapperService mapperService,
			MappingMetadata metadata,
			StructDefinitionService definitionService) { //@formatter:on

		this.mappedClass = mappingClass;
		this.mapperService = mapperService;
		this.metadata = metadata;
		this.definitionService = definitionService;
		this.nameNormalizer = x -> x.toUpperCase().replaceAll("_", StringUtils.EMPTY);
	}

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.mapper.StructMapper#toStruct(java.lang.Object, java.sql.Connection)
	 */
	@Override
	public STRUCT toStruct(@NonNull T source, Connection conn) throws SQLException {

		Assert.isTrue(mappedClass.equals(source.getClass()),
			"Expected " + mappedClass.getName() + ", found " + source.getClass().getName());

		log.trace("Converting {} to struct", source);

		//@formatter:off
		StructMetadata structData = metadata.getStructs().stream()
			.filter(x -> x.getMappedClass().equals(source.getClass()))
			.findFirst()
			.orElseThrow(() -> new OsmMappingException("Missing metadata for source class " + source.getClass().getName()));
		//@formatter:on

		BeanWrapper sourceBeanWrapper = new BeanWrapperImpl(source);
		int structSize = structData.getFields().size();

		Object[] values = new Object[structSize];
		for (int i = 0; i < structSize; i++) {
			FieldMetadata mappingField = structData.getFields().get(i);
			values[i] = resolveMappedValue(mappingField, sourceBeanWrapper, conn);
		}
		try {
			StructDescriptor descriptor = definitionService.structDescriptor(structData.getStrucyName(), conn);
			return new STRUCT(descriptor, conn, values);
		}
		catch (SQLException ex) {
			throw new OsmMappingException(String.format("Error mapping class %s", mappedClass.getName()), ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.mapper.StructMapper#fromStruct(oracle.sql.STRUCT)
	 */
	@Override
	public T fromStruct(@NonNull STRUCT struct) throws SQLException {
		log.trace("Converting struct {} to mapped class {}", struct.getSQLTypeName(), mappedClass.getName());

		T mappedObject = BeanUtils.instantiateClass(mappedClass);
		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);

		ResultSetMetaData rsmd = struct.getDescriptor().getMetaData();
		int columnCount = rsmd.getColumnCount();
		Object[] attributes = struct.getAttributes();

		//@formatter:off
		StructMetadata mappingStructData = metadata.getStructs().stream()
			.filter(x -> x.getMappedClass().equals(mappedClass))
			.findFirst()
			.orElseThrow(() -> new OsmMappingException("Missing struct mapping data for class " + mappedClass.getName()));
		//@formatter:on

		for (int index = 0; index < columnCount; index++) {
			String columnName = JdbcUtils.lookupColumnName(rsmd, index + 1).toLowerCase();
			String columnNameNormalized = nameNormalizer.apply(columnName);

			Optional<FieldMetadata> optionalMappingField = mappingStructData.getFields().stream()
				.filter(x -> columnNameNormalized.equals(nameNormalizer.apply(x.getOracleColumnName()))).findFirst();

			if (optionalMappingField.isPresent()) {
				Object value = attributes[index];
				FieldMetadata mappedField = optionalMappingField.get();
				setEntityProperty(beanWrapper, mappedField.getJavaAttributeName(), value);
			}
			else {
				log.warn("Missing mapping {} in class {}", columnName, mappedClass.getName());
			}
		}
		return mappedObject;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object resolveMappedValue(FieldMetadata mappingField, BeanWrapper sourceBeanWrapper, Connection connection)
		throws SQLException {

		Object result = null;
		if (!mappingField.isMapped()) {
			log.warn("Unmapped field {} in {}", mappingField.getOracleColumnName(), mappedClass.getName());
		}
		else {
			String javaFieldName = mappingField.getJavaAttributeName();
			if (sourceBeanWrapper.isReadableProperty(javaFieldName)) {
				log.trace("Mapped {} to field {}", javaFieldName, mappingField.getOracleColumnName());
				result = sourceBeanWrapper.getPropertyValue(javaFieldName);

				if (result != null) {

					if (Date.class.isAssignableFrom(result.getClass())) {
						return new java.sql.Date(((Date) result).getTime());
					}
					else if (OracleArray.class.getName().equals(mappingField.getOracleColumnClassName())) {
						log.debug("Detected oracle list mapping");
						Assert.isTrue(List.class.isAssignableFrom(result.getClass()), "Expected list");
						String oracleCollectionName = mappingField.getOracleTypeName();
						ArrayMapper arrayMapper = mapperService.arrayMapper(mappedClass, oracleCollectionName);
						result = arrayMapper.toArray((List) result, connection);
					}
					else {
						Class<?> resultClass = result.getClass();
						StructMetadata structCandidate = metadata.getStructs().stream()
							.filter(x -> resultClass.equals(x.getMappedClass())).findFirst().orElseGet(() -> null);
						if (structCandidate == null) {
							return result;
						}
						else {
							log.debug("Mappping internal value to struct as {}", resultClass.getName());
							StructMapper mapper = mapperService.mapper(resultClass);
							result = mapper.toStruct(result, connection);
						}
					}
				}
			}
			else {
				log.warn("No readable property {} in {}", javaFieldName,
					sourceBeanWrapper.getWrappedInstance().getClass().getName());
			}
		}
		return result;
	}

	private void setEntityProperty(BeanWrapper beanWrapper, String attributeName, Object value) {
		if (value == null) {
			beanWrapper.setPropertyValue(attributeName, null);
			return;
		}
		try {
			Field field = beanWrapper.getWrappedInstance().getClass().getDeclaredField(attributeName);
			OracleCollection oracleCollection = field.getAnnotation(OracleCollection.class);
			if (oracleCollection != null) {
				// Internal array conversions
				if (value != null) {
					Assert.isInstanceOf(ARRAY.class, value);
					String collectionName = oracleCollection.value();
					ParameterizedType parametrizedType = (ParameterizedType) field.getGenericType();
					Class<?> entityClass = (Class<?>) parametrizedType.getActualTypeArguments()[0];
					ArrayMapper<?> arrayMapper = mapperService.arrayMapper(entityClass, collectionName);
					List<?> list = arrayMapper.fromArray((ARRAY) value);
					beanWrapper.setPropertyValue(attributeName, list);
				}
			}
			else if (STRUCT.class.isAssignableFrom(value.getClass())) {
				Class<?> entityClass = field.getType();
				StructMapper<?> mapper = mapperService.mapper(entityClass);
				Object result = mapper.fromStruct((STRUCT) value);
				beanWrapper.setPropertyValue(attributeName, result);
			}
			else {
				beanWrapper.setPropertyValue(attributeName, value);
			}
		}
		catch (Exception ex) {
			log.error("Cant set property value {}: {}", attributeName, value, ex);
		}
	}
}
