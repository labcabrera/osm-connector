package org.lab.osm.connector.mapper.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.lab.osm.connector.exception.OsmMappingException;
import org.lab.osm.connector.mapper.ArrayMapper;
import org.lab.osm.connector.mapper.StructDefinitionService;
import org.lab.osm.connector.mapper.StructMapper;
import org.lab.osm.connector.metadata.model.MappingMetadata;
import org.lab.osm.connector.metadata.model.StructMetadata;
import org.lab.osm.connector.service.StructMapperService;

import lombok.extern.slf4j.Slf4j;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

@Slf4j
public class MetadataArrayMapper<T> implements ArrayMapper<T> {

	private final Class<T> mappedClass;
	private final String oracleCollectionName;
	private final StructMapperService mapperService;
	private final MappingMetadata mappingMetadata;
	private final StructDefinitionService definitionService;

	public MetadataArrayMapper( //@formatter:off
			Class<T> mappedClass,
			String oracleCollectionName,
			StructMapperService mapperService,
			MappingMetadata mappingMetadata,
			StructDefinitionService definitionService) { //@formatter:on
		this.mappedClass = mappedClass;
		this.oracleCollectionName = oracleCollectionName;
		this.mapperService = mapperService;
		this.mappingMetadata = mappingMetadata;
		this.definitionService = definitionService;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ARRAY toArray(List<T> list, Connection conn) throws SQLException {
		log.trace("Mapping list of {} to Oracle ARRAY", mappedClass.getSimpleName());

		Object[] values = new Object[list.size()];
		Object firstNotNull = list.stream().filter(x -> x != null).findFirst().orElseGet(() -> null);
		if (firstNotNull != null) {
			StructMetadata itemMappingField = mappingMetadata.getStructs().stream()
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
						// Recursive STRUCT conversion
						values[i] = mapper.toStruct(sourceListValue, conn);
					}
					else {
						// Direct reference value
						values[i] = sourceListValue;
					}
				}
				else {
					values[i] = null;
				}
			}
		}
		ArrayDescriptor arrayDescriptor = definitionService.arrayDescriptor(oracleCollectionName, conn);
		return new ARRAY(arrayDescriptor, conn, values);

	}

	@Override
	public List<T> fromArray(ARRAY array) throws SQLException {
		log.trace("Mapping Oracle ARRAY to java List of {}", mappedClass.getSimpleName());
		throw new NotImplementedException("Not implemented");
	}

}
