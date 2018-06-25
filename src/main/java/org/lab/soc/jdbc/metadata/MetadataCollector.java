package org.lab.soc.jdbc.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.lab.soc.annotation.OracleCollection;
import org.lab.soc.annotation.OracleField;
import org.lab.soc.annotation.OracleStruct;
import org.lab.soc.jdbc.metadata.model.OracleMappingData;
import org.lab.soc.jdbc.metadata.model.OracleMappingField;
import org.lab.soc.jdbc.metadata.model.OracleMappingStructData;
import org.reflections.Reflections;

import lombok.extern.slf4j.Slf4j;
import oracle.sql.StructDescriptor;

@Slf4j
public class MetadataCollector {

	private final DataSource dataSource;

	public MetadataCollector(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public OracleMappingData readMetadata(String packageName) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			return readMetadata(packageName, connection);
		}
	}

	public OracleMappingData readMetadata(String packageName, Connection connection) throws SQLException {

		Set<Class<?>> structs = new Reflections(packageName).getTypesAnnotatedWith(OracleStruct.class);

		OracleMappingData result = new OracleMappingData();
		result.setPackageName(packageName);

		for (Class<?> structClass : structs) {
			log.info("Loading class {}", structClass.getName());

			result.register(loadStructData(structClass, connection));

		}
		return result;

	}

	private OracleMappingStructData loadStructData(Class<?> structClass, Connection connection) throws SQLException {
		OracleMappingStructData result = new OracleMappingStructData();
		OracleStruct annotation = structClass.getAnnotation(OracleStruct.class);
		String structName = annotation.value();
		mapOracleMetaData(result, structName, connection);
		mapFields(result, structClass);
		result.setMappedClass(structClass);
		result.setStrucyName(annotation.value());
		return result;
	}

	private void mapOracleMetaData(OracleMappingStructData result, String structName, Connection connection)
		throws SQLException {
		StructDescriptor desc = new StructDescriptor(structName, connection);
		ResultSetMetaData metaData = desc.getMetaData();
		int count = metaData.getColumnCount();
		for (int i = 0; i < count; i++) {
			OracleMappingField field = new OracleMappingField();
			field.setMapped(false);
			field.setOracleColumnName(metaData.getColumnName(i + 1));
			field.setOracleTypeName(metaData.getColumnTypeName(i + 1));
			field.setOracleColumnClassName(metaData.getColumnClassName(i + 1));
			field.setOracleSchemaName(metaData.getSchemaName(i + 1));
			result.registerField(field);
		}
	}

	private void mapFields(OracleMappingStructData data, Class<?> structClass) {
		UnaryOperator<String> nameNormalizer = x -> x.toUpperCase().replaceAll("_", "");

		for (Field field : structClass.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				log.trace("Ignoring static field {}", field.getName());
				continue;
			}

			String fieldName = field.getName();
			OracleField oracleField = field.getAnnotation(OracleField.class);
			OracleCollection oracleCollection = field.getAnnotation(OracleCollection.class);

			OracleMappingField target = null;

			if (oracleCollection != null) {
				String collectionName = oracleCollection.value();

				String fieldNameMatch = nameNormalizer.apply(fieldName);
				Predicate<OracleMappingField> fieldPredicate = x -> fieldNameMatch
					.equals(nameNormalizer.apply(x.getOracleColumnName()));

				log.debug("Mapping field '{}' as a collection '{}'", fieldName, collectionName);

				List<OracleMappingField> collect = data.getFields().stream().filter(fieldPredicate)
					.collect(Collectors.toList());

				switch (collect.size()) {
				case 1:
					target = collect.iterator().next();
					log.debug("Oracle bind {}", target.getOracleColumnName());
					bindFieldInfo(target, field);
					target.setMapped(true);
					break;
				case 0:
					OracleMappingField newMapping = new OracleMappingField();
					bindFieldInfo(newMapping, field);
					data.registerUnmappedField(newMapping);
					break;
				default:
					throw new RuntimeException("Multiple candidates for field " + field.getName() + "("
						+ field.getDeclaringClass().getName() + ")");
				}

				bindFieldInfo(target, field);
				target.setMapped(true);

				// TODO map collection info
			}
			else {

				String fieldNameMatch;
				Predicate<OracleMappingField> fieldPredicate;
				if (oracleField != null) {
					fieldNameMatch = nameNormalizer.apply(oracleField.value());
				}
				else {
					fieldNameMatch = nameNormalizer.apply(fieldName);
				}
				fieldPredicate = x -> fieldNameMatch.equals(nameNormalizer.apply(x.getOracleColumnName()));

				String oracleName = oracleField.value();
				log.debug("Mapping field '{}' as '{}'", fieldName, oracleName);

				List<OracleMappingField> collect = data.getFields().stream().filter(fieldPredicate)
					.collect(Collectors.toList());

				switch (collect.size()) {
				case 1:
					target = collect.iterator().next();
					log.debug("Oracle bind {}", target.getOracleColumnName());
					bindFieldInfo(target, field);
					target.setMapped(true);
					break;
				case 0:
					OracleMappingField newMapping = new OracleMappingField();
					bindFieldInfo(newMapping, field);
					data.registerUnmappedField(newMapping);
					break;
				default:
					throw new RuntimeException("Multiple candidates for field " + field.getName() + "("
						+ field.getDeclaringClass().getName() + ")");
				}

				bindFieldInfo(target, field);
				target.setMapped(true);

			}
			// else {
			// log.debug("Mapping field as default");
			//
			// List<OracleMappingField> collect = data.getFields().stream().filter(fieldPredicate)
			// .collect(Collectors.toList());
			//
			// switch (collect.size()) {
			// case 1:
			// target = collect.iterator().next();
			// log.debug("Oracle bind {}", target.getOracleColumnName());
			// bindFieldInfo(target, field);
			// target.setMapped(true);
			// break;
			// case 0:
			// OracleMappingField newMapping = new OracleMappingField();
			// bindFieldInfo(newMapping, field);
			// data.registerUnmappedField(newMapping);
			// break;
			// default:
			// throw new RuntimeException("Multiple candidates for field " + field.getName() + "("
			// + field.getDeclaringClass().getName() + ")");
			// }
			//
			// }

		}
	}

	private void bindFieldInfo(OracleMappingField mapping, Field field) {
		mapping.setJavaAttributeName(field.getName());

	}

}
