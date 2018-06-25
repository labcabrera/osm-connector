package org.lab.osm.connector.mapper.service;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.lab.osm.connector.mapper.StructDefinitionService;
import org.lab.osm.connector.metadata.MetadataCollector;
import org.lab.osm.connector.metadata.model.OracleMappingData;
import org.springframework.data.jdbc.support.oracle.StructMapper;

public class MetadataStructMapperService implements StructMapperService {

	private final OracleMappingData metadata;
	private final StructDefinitionService definitionService;

	// TODO allow multiple packages
	public MetadataStructMapperService(DataSource dataSource, StructDefinitionService definitionService,
		String packageName) throws SQLException {
		MetadataCollector collector = new MetadataCollector(dataSource);
		this.metadata = collector.readMetadata(packageName);
		this.definitionService = definitionService;
	}

	@Override
	public <T> StructMapper<T> mapper(Class<T> mappedClass) {
		return new MetadataStructMapper<>(mappedClass, this, metadata, definitionService);
	}

}
