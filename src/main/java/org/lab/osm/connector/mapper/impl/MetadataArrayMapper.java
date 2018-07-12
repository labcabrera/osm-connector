package org.lab.osm.connector.mapper.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.lab.osm.connector.exception.OsmMappingException;
import org.lab.osm.connector.mapper.ArrayMapper;
import org.lab.osm.connector.mapper.StructDefinitionService;
import org.lab.osm.connector.mapper.StructMapper;
import org.lab.osm.connector.mapper.StructMapperService;
import org.lab.osm.connector.metadata.model.MappingMetadata;
import org.lab.osm.connector.metadata.model.StructMetadata;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.STRUCT;

/**
 * 
 * Default {@link ArrayMapper} using metadata information from the entity model annotations.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Slf4j
public class MetadataArrayMapper<T> implements ArrayMapper<T> {

	private final Class<T> mappedClass;
	private final String oracleCollectionName;
	private final StructMapperService mapperService;
	private final MappingMetadata mappingMetadata;
	private final StructDefinitionService definitionService;

	/**
	 * Public constructor.
	 * 
	 * @param mappedClass
	 * @param oracleCollectionName
	 * @param mapperService
	 * @param mappingMetadata
	 * @param definitionService
	 */
	public MetadataArrayMapper( //@formatter:off
			@NonNull Class<T> mappedClass,
			@NonNull String oracleCollectionName,
			@NonNull StructMapperService mapperService,
			@NonNull MappingMetadata mappingMetadata,
			@NonNull StructDefinitionService definitionService) { //@formatter:on
		this.mappedClass = mappedClass;
		this.oracleCollectionName = oracleCollectionName;
		this.mapperService = mapperService;
		this.mappingMetadata = mappingMetadata;
		this.definitionService = definitionService;
	}

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.mapper.ArrayMapper#toArray(java.util.List, java.sql.Connection)
	 */
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

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.mapper.ArrayMapper#fromArray(oracle.sql.ARRAY)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<T> fromArray(ARRAY array) throws SQLException {
		log.trace("Mapping Oracle ARRAY to java List of {}", mappedClass.getSimpleName());
		if (array == null) {
			return null;
		}

		long t0 = System.currentTimeMillis();
		Object[] values = (Object[]) array.getArray();
		long t = System.currentTimeMillis() - t0;
		if (t > 0) {
			log.trace("Geting array objets took {} ms", t);
		}

		List<T> list = new ArrayList<>();
		StructMapper mapper = mapperService.mapper(mappedClass);
		for (int z = 0; z < values.length; z++) {
			STRUCT struct = (STRUCT) values[z];
			T p = (T) mapper.fromStruct(struct);
			list.add(p);
		}
		return list;
	}

}
