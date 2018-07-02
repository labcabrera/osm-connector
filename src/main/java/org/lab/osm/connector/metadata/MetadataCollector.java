package org.lab.osm.connector.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;
import java.util.function.UnaryOperator;

import javax.sql.DataSource;

import org.lab.osm.connector.annotation.OracleCollection;
import org.lab.osm.connector.annotation.OracleField;
import org.lab.osm.connector.annotation.OracleStruct;
import org.lab.osm.connector.exception.OsmMappingException;
import org.lab.osm.connector.metadata.model.FieldMetadata;
import org.lab.osm.connector.metadata.model.MappingMetadata;
import org.lab.osm.connector.metadata.model.StructMetadata;
import org.reflections.Reflections;

import lombok.extern.slf4j.Slf4j;
import oracle.sql.StructDescriptor;

@Slf4j
public class MetadataCollector {

	private final DataSource dataSource;
	private final UnaryOperator<String> nameNormalizer = x -> x.toUpperCase().replaceAll("_", "");

	public MetadataCollector(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void readMetadata(MappingMetadata metadata, String packageName) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			readMetadata(metadata, packageName, connection);
		}
	}

	public void readMetadata(MappingMetadata metadata, String packageName, Connection connection) throws SQLException {
		Set<Class<?>> structs = new Reflections(packageName).getTypesAnnotatedWith(OracleStruct.class);
		metadata.registerPackageName(packageName);
		for (Class<?> structClass : structs) {
			log.info("Loading class {}", structClass.getName());
			metadata.register(loadStructData(structClass, connection));
		}
	}

	private StructMetadata loadStructData(Class<?> structClass, Connection connection) throws SQLException {
		StructMetadata result = new StructMetadata();
		OracleStruct annotation = structClass.getAnnotation(OracleStruct.class);
		String structName = annotation.value();
		mapOracleMetaData(result, structName, connection);
		mapReflectionFields(result, structClass);
		result.setMappedClass(structClass);
		result.setStrucyName(annotation.value());
		return result;
	}

	private void mapOracleMetaData(StructMetadata result, String structName, Connection connection) {
		try {
			StructDescriptor desc = new StructDescriptor(structName, connection);
			ResultSetMetaData metaData = desc.getMetaData();
			int count = metaData.getColumnCount();
			for (int i = 0; i < count; i++) {
				FieldMetadata field = new FieldMetadata();
				field.setMapped(false);
				field.setOracleColumnName(metaData.getColumnName(i + 1));
				field.setOracleTypeName(metaData.getColumnTypeName(i + 1));
				field.setOracleColumnClassName(metaData.getColumnClassName(i + 1));
				field.setOracleSchemaName(metaData.getSchemaName(i + 1));
				result.registerField(field);
			}
		}
		catch (SQLException ex) {
			throw new OsmMappingException("Error reading Oracle metadata for Struct " + structName, ex);
		}
	}

	private void mapReflectionFields(StructMetadata data, Class<?> structClass) {

		for (Field field : structClass.getDeclaredFields()) {

			// Skip static fields
			if (Modifier.isStatic(field.getModifiers())) {
				log.trace("Ignoring static field {}", field.getName());
				continue;
			}

			String fieldName = field.getName();
			OracleField oracleField = field.getAnnotation(OracleField.class);
			OracleCollection oracleCollection = field.getAnnotation(OracleCollection.class);

			if (oracleCollection != null) {
				// Oracle collection binding
				bindOracleCollection(oracleCollection.value(), field, data);
			}
			else {
				// Common field binding
				String fieldNameMatch;
				if (oracleField != null) {
					fieldNameMatch = nameNormalizer.apply(oracleField.value());
				}
				else {
					fieldNameMatch = nameNormalizer.apply(fieldName);
				}
				FieldMetadata target = data.getFields().stream()
					.filter(x -> fieldNameMatch.equals(nameNormalizer.apply(x.getOracleColumnName()))).findFirst()
					.orElseGet(() -> null);
				if (target != null) {
					log.trace("Oracle bind {}", target.getOracleColumnName());
					target.setMapped(true);
				}
				else {
					target = new FieldMetadata();
					target.setMapped(false);
					data.registerUnmappedField(target);
				}
				bindFieldInfo(target, field);
			}
		}
	}

	private void bindOracleCollection(String collectionName, Field field, StructMetadata data) {
		String fieldName = field.getName();
		String fieldNameNormalized = nameNormalizer.apply(fieldName);
		log.trace("Mapping field '{}' as a collection '{}'", fieldName, collectionName);

		FieldMetadata fieldMetadata = data.getFields().stream()
			.filter(x -> fieldNameNormalized.equals(nameNormalizer.apply(x.getOracleColumnName()))).findFirst()
			.orElseGet(() -> null);

		if (fieldMetadata != null) {
			log.trace("Binded collection {} to field {}", fieldMetadata.getOracleColumnName(), fieldName);
			fieldMetadata.setMapped(true);
		}
		else {
			log.trace("Field {} is not present in parent oracle mapping", fieldName);
			fieldMetadata = new FieldMetadata();
			fieldMetadata.setMapped(false);
			data.registerUnmappedField(fieldMetadata);
		}
		bindFieldInfo(fieldMetadata, field);
	}

	private void bindFieldInfo(FieldMetadata mapping, Field field) {
		mapping.setJavaAttributeName(field.getName());
	}

}
