package org.lab.osm.connector.service;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.lab.osm.connector.mapper.ArrayMapper;
import org.lab.osm.connector.mapper.StructDefinitionService;
import org.lab.osm.connector.mapper.StructMapper;
import org.lab.osm.connector.mapper.impl.MetadataArrayMapper;
import org.lab.osm.connector.mapper.impl.MetadataStructMapper;
import org.lab.osm.connector.metadata.MetadataCollector;
import org.lab.osm.connector.metadata.model.MappingMetadata;

import lombok.Getter;

/**
 * <code>StructMapperService</code> using Oracle metadata.
 * 
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
public class MetadataStructMapperService implements StructMapperService {

	@Getter
	private final MappingMetadata metadata;

	private final StructDefinitionService definitionService;

	/**
	 * Public constructor.
	 * @param dataSource
	 * @param definitionService
	 * @param packageName
	 * @throws SQLException
	 */
	public MetadataStructMapperService( //@formatter:off
			DataSource dataSource,
			StructDefinitionService definitionService,
			MetadataCollector metadataCollector,
			String[] packageNames) throws SQLException { //@formatter:on
		this.metadata = new MappingMetadata();
		this.definitionService = definitionService;
		for (String packageName : packageNames) {
			metadataCollector.readMetadata(metadata, packageName);
		}
	}

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.service.StructMapperService#mapper(java.lang.Class)
	 */
	@Override
	public <T> StructMapper<T> mapper(Class<T> mappedClass) {
		return new MetadataStructMapper<>(mappedClass, this, metadata, definitionService);
	}

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.service.StructMapperService#listMapper(java.lang.Class)
	 */
	@Override
	public <T> ArrayMapper<T> arrayMapper(Class<T> mappedClass, String oracleCollectionName) {
		return new MetadataArrayMapper<>(mappedClass, oracleCollectionName, this, metadata, definitionService);
	}

}
