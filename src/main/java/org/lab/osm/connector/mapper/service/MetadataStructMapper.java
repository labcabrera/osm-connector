package org.lab.osm.connector.mapper.service;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.lab.osm.connector.exception.OsmMappingException;
import org.lab.osm.connector.mapper.StructDefinitionService;
import org.lab.osm.connector.metadata.model.OracleMappingData;
import org.lab.osm.connector.metadata.model.OracleMappingField;
import org.lab.osm.connector.metadata.model.OracleMappingStructData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.jdbc.support.oracle.StructMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

@Slf4j
public class MetadataStructMapper<T> implements StructMapper<T> {

	private final Class<T> mappedClass;
	private final OracleMappingData metadata;
	private final StructDefinitionService definitionService;
	private final UnaryOperator<String> nameNormalizer;

	public MetadataStructMapper(Class<T> mappingClass, OracleMappingData metadata,
		StructDefinitionService definitionService) {

		this.mappedClass = mappingClass;
		this.metadata = metadata;
		this.definitionService = definitionService;
		// TODO use service
		this.nameNormalizer = x -> x.toUpperCase().replaceAll("_", "");
	}

	@Override
	public STRUCT toStruct(T source, Connection conn, String typeName) throws SQLException {
		Assert.notNull(source, "Required source");

		log.debug("Converting {} to struct (using typeName {})", source, typeName);

		//@formatter:off
		OracleMappingStructData structData = metadata.getStructs().stream()
			.filter(x -> x.getMappedClass().equals(source.getClass()))
			.findFirst()
			.orElseThrow(() -> new OsmMappingException("Missing metadata for source class " + source.getClass().getName()));
		//@formatter:on

		int structSize = structData.getFields().size();

		Object[] values = new Object[structSize];
		for (int i = 0; i < structSize; i++) {
			OracleMappingField mappingField = structData.getFields().get(i);
			Object value = null;
			if (mappingField.getMapped()) {
				// TODO check recursive conversion
				String javaFieldName = mappingField.getJavaAttributeName();
				BeanWrapper beanWrapper = new BeanWrapperImpl(source);
				if (beanWrapper.isReadableProperty(javaFieldName)) {
					log.debug("Mapped {} to field {}", javaFieldName, mappingField.getOracleColumnName());
					value = beanWrapper.getPropertyValue(javaFieldName);
				}
				else {
					log.warn("No readable property {} in {}", javaFieldName, source.getClass().getName());
				}
			}
			else {
				log.warn("Unmapped field {} in {}", mappingField.getOracleColumnName(), source.getClass().getName());
			}
			values[i] = value;
		}
		StructDescriptor descriptor = definitionService.structDescriptor(typeName, conn);
		return new STRUCT(descriptor, conn, values);
	}

	@Override
	public T fromStruct(STRUCT struct) throws SQLException {
		log.debug("Converting struct {} to mapped class {}", struct.getSQLTypeName(), mappedClass.getName());

		T mappedObject = BeanUtils.instantiateClass(mappedClass);
		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);

		ResultSetMetaData rsmd = struct.getDescriptor().getMetaData();
		int columnCount = rsmd.getColumnCount();
		Object[] attributes = struct.getAttributes();

		//@formatter:off
		OracleMappingStructData mappingStructData = metadata.getStructs().stream()
			.filter(x -> x.getMappedClass().equals(mappedClass))
			.findFirst()
			.orElseThrow(() -> new OsmMappingException("Missing struct mapping data for class " + mappedClass.getName()));
		//@formatter:on

		for (int index = 0; index < columnCount; index++) {
			String columnName = JdbcUtils.lookupColumnName(rsmd, index + 1).toLowerCase();
			String columnNameNormalized = nameNormalizer.apply(columnName);

			//@formatter:off
			Optional<OracleMappingField> optionalMappingField = mappingStructData.getFields().stream()
				.filter(x -> columnNameNormalized.equals(nameNormalizer.apply(x.getOracleColumnName())))
				.findFirst();
			//@formatter:on

			if (optionalMappingField.isPresent()) {
				Object value = attributes[index];
				OracleMappingField mappedField = optionalMappingField.get();
				beanWrapper.setPropertyValue(mappedField.getJavaAttributeName(), value);
			}
			else {
				log.warn("Missing mapping {} in class {}", columnName, mappedClass.getName());
			}
		}
		return mappedObject;
	}

}
