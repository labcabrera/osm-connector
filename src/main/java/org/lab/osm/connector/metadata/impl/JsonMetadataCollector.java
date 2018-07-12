package org.lab.osm.connector.metadata.impl;

import java.io.File;
import java.io.FileInputStream;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.lab.osm.connector.exception.OsmConnectorException;
import org.lab.osm.connector.metadata.model.MappingMetadata;
import org.lab.osm.connector.metadata.model.StructMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link org.lab.osm.connector.metadata.MetadataCollector} reading information from a JSON file. If file is not present
 * it will be generated after read the model entities.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
public class JsonMetadataCollector extends DefaultMetadataCollector {

	private final File folder;
	private final String filePrefix;
	private final ObjectMapper objectMapper;

	/**
	 * Public constructor.
	 * 
	 * @param dataSource
	 * @param objectMapper
	 * @param jsonFolder
	 * @param filePrefix
	 */
	public JsonMetadataCollector(DataSource dataSource, ObjectMapper objectMapper, String jsonFolder,
		String filePrefix) {
		super(dataSource);
		this.folder = new File(jsonFolder);
		this.filePrefix = filePrefix;
		this.objectMapper = objectMapper;

		if (!folder.exists() && !folder.mkdirs()) {
			throw new OsmConnectorException("Can not create folder " + folder.getAbsolutePath());
		}
		if (!folder.canRead()) {
			throw new OsmConnectorException("Can not read folder " + folder.getAbsolutePath());
		}
	}

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.metadata.DefaultMetadataCollector#readMetadata(org.lab.osm.connector.metadata.model.MappingMetadata, java.lang.String)
	 */
	@Override
	public void readMetadata(MappingMetadata metadata, String packageName) {
		File file = getJsonFile(packageName);
		if (file.exists()) {
			readMetadataFromFile(metadata, packageName, file);
		}
		else {
			super.readMetadata(metadata, packageName);
			writeMetadataToFile(metadata, file);
		}
	}

	private void readMetadataFromFile(MappingMetadata metadata, String packageName, File file) {
		try {
			try (FileInputStream in = new FileInputStream(file)) {
				MappingMetadata readed = objectMapper.readValue(in, MappingMetadata.class);
				metadata.getPackageNames().add(packageName);
				for (StructMetadata i : readed.getStructs()) {
					metadata.getStructs().add(i);
				}
			}
		}
		catch (Exception ex) {
			throw new OsmConnectorException("Cant read metadata from file", ex);
		}
	}

	private void writeMetadataToFile(MappingMetadata metadata, File file) {
		try {
			objectMapper.writeValue(file, metadata);
		}
		catch (Exception ex) {
			throw new OsmConnectorException("Error writing metadata", ex);
		}
	}

	private File getJsonFile(String packageName) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(filePrefix)) {
			sb.append(filePrefix).append("-");
		}
		sb.append("metadata-");
		sb.append(packageName.replaceAll("\\.", "-"));
		sb.append(".json");
		return new File(folder, sb.toString());
	}

}
