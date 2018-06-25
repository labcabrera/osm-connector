package org.lab.soc.jdbc.mapper;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.lab.soc.jdbc.StructDefinitionService;
import org.lab.soc.jdbc.metadata.model.OracleMappingData;
import org.lab.soc.jdbc.metadata.model.OracleMappingField;
import org.lab.soc.jdbc.metadata.model.OracleMappingStructData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.jdbc.support.oracle.StructMapper;
import org.springframework.jdbc.support.JdbcUtils;

import lombok.extern.slf4j.Slf4j;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

@Slf4j
public class MetadataStructMapper<T> implements StructMapper<T> {

	private final Class<T> mappedClass;
	private final OracleMappingData metadata;
	private final StructDefinitionService definitionService;

	public MetadataStructMapper(Class<T> mappingClass, OracleMappingData metadata,
		StructDefinitionService definitionService) {
		this.mappedClass = mappingClass;
		this.metadata = metadata;
		this.definitionService = definitionService;
	}

	@Override
	public STRUCT toStruct(T source, Connection conn, String typeName) throws SQLException {
		log.debug("Converting {} to struct (using typeName {})", source, typeName);

		OracleMappingStructData structData = metadata.getStructs().stream()
			.filter(x -> x.getMappedClass().equals(source.getClass())).findFirst().get();

		int structSize = structData.getFields().size();

		Object[] values = new Object[structSize];
		// TODO check struct order
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
		T mappedObject = BeanUtils.instantiateClass(mappedClass);
		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);

		String typeName = struct.getSQLTypeName();
		log.debug("Converting struct {} to {}", typeName, mappedClass.getName());
		ResultSetMetaData rsmd = struct.getDescriptor().getMetaData();

		OracleMappingStructData mappingStructData = metadata.findStructByMappingClass(mappedClass)
			.orElseThrow(() -> new RuntimeException("Missing struct mapping data for class " + mappedClass.getName()));

		UnaryOperator<String> nameNormalizer = x -> x.toUpperCase().replaceAll("_", "");

		Object[] attributes = struct.getAttributes();

		int columnCount = rsmd.getColumnCount();
		for (int index = 0; index < columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index + 1).toLowerCase();

			// TODO use service
			String normalizedName = nameNormalizer.apply(column);
			Predicate<OracleMappingField> fieldPredicate = x -> normalizedName
				.equals(nameNormalizer.apply(x.getOracleColumnName()));

			Optional<OracleMappingField> optionalMappingField = mappingStructData.getFields().stream()
				.filter(fieldPredicate).findFirst();

			if (optionalMappingField.isPresent()) {
				Object value = attributes[index];
				OracleMappingField mappedField = optionalMappingField.get();
				beanWrapper.setPropertyValue(mappedField.getJavaAttributeName(), value);
			}
			else {
				log.warn("Missing mapping {} in class {}", column, mappedClass.getName());
			}

		}
		return mappedObject;
	}

}
