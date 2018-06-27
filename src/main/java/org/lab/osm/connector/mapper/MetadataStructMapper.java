package org.lab.osm.connector.mapper;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;
import org.lab.osm.connector.exception.OsmMappingException;
import org.lab.osm.connector.metadata.model.FieldMetadata;
import org.lab.osm.connector.metadata.model.MappingMetadata;
import org.lab.osm.connector.metadata.model.StructMetadata;
import org.lab.osm.connector.service.StructMapperService;
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
import oracle.sql.ArrayDescriptor;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

@Slf4j
public class MetadataStructMapper<T> implements StructMapper<T> {

	private final Class<T> mappedClass;
	private final StructMapperService mapperService;
	private final MappingMetadata metadata;
	private final StructDefinitionService definitionService;

	// TODO use customizable service
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

	@Override
	public T fromStruct(@NonNull STRUCT struct) throws SQLException {
		log.debug("Converting struct {} to mapped class {}", struct.getSQLTypeName(), mappedClass.getName());

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

			//@formatter:off
			Optional<FieldMetadata> optionalMappingField = mappingStructData.getFields().stream()
				.filter(x -> columnNameNormalized.equals(nameNormalizer.apply(x.getOracleColumnName())))
				.findFirst();
			//@formatter:on

			if (optionalMappingField.isPresent()) {
				Object value = attributes[index];
				FieldMetadata mappedField = optionalMappingField.get();
				beanWrapper.setPropertyValue(mappedField.getJavaAttributeName(), value);
			}
			else {
				log.warn("Missing mapping {} in class {}", columnName, mappedClass.getName());
			}
		}
		return mappedObject;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object resolveMappedValue(FieldMetadata mappingField, BeanWrapper sourceBeanWrapper,
		Connection connection) throws SQLException {

		Object result = null;
		if (!mappingField.isMapped()) {
			log.warn("Unmapped field {} in {}", mappingField.getOracleColumnName(), mappedClass.getName());
		}
		else {
			String javaFieldName = mappingField.getJavaAttributeName();
			if (sourceBeanWrapper.isReadableProperty(javaFieldName)) {
				log.debug("Mapped {} to field {}", javaFieldName, mappingField.getOracleColumnName());
				result = sourceBeanWrapper.getPropertyValue(javaFieldName);

				if (result != null) {

					if (Date.class.isAssignableFrom(result.getClass())) {
						return new java.sql.Date(((Date) result).getTime());
					}
					else if (OracleArray.class.getName().equals(mappingField.getOracleColumnClassName())) {
						log.debug("Detected oracle list mapping");
						Assert.isTrue(List.class.isAssignableFrom(result.getClass()), "Expected list");
						result = resolveMappedArrayValue(mappingField, (List) result, connection);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object resolveMappedArrayValue(FieldMetadata mappingField, List list, Connection connection)
		throws SQLException {
		Object[] values = new Object[list.size()];
		Object firstNotNull = list.stream().filter(x -> x != null).findFirst().orElseGet(() -> null);
		if (firstNotNull != null) {
			StructMetadata itemMappingField = this.metadata.getStructs().stream()
				.filter(x -> firstNotNull.getClass().equals(x.getMappedClass())).findFirst()
				.orElseThrow(() -> new OsmMappingException("Missing metadata"));
			StructMapper mapper = null;
			if (itemMappingField != null) {
				mapper = this.mapperService.mapper(firstNotNull.getClass());
			}

			for (int i = 0; i < list.size(); i++) {
				Object sourceListValue = list.get(i);
				if (sourceListValue != null) {
					if (mapper != null) {
						// recursive STRUCT conversion
						values[i] = mapper.toStruct(sourceListValue, connection);
					}
					else {
						// direct reference
						values[i] = sourceListValue;
					}

				}
				else {
					values[i] = null;
				}
			}

		}
		String collectionName = mappingField.getOracleTypeName();
		ArrayDescriptor arrayDescriptor = definitionService.arrayDescriptor(collectionName, connection);
		ARRAY oracleArray = new ARRAY(arrayDescriptor, connection, values);
		return oracleArray;
	}

}
