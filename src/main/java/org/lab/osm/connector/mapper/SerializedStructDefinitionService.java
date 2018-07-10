package org.lab.osm.connector.mapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.lab.osm.connector.exception.OsmConnectorException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import oracle.sql.ArrayDescriptor;
import oracle.sql.StructDescriptor;

@Slf4j
public class SerializedStructDefinitionService implements StructDefinitionService {

	private String FILE_EXT = ".ser";

	private final File folder;
	private final String filePrefix;
	private final Map<String, StructDescriptor> structDescriptorValues;
	private final Map<String, ArrayDescriptor> arrayDescriptorValues;

	public SerializedStructDefinitionService(String serializedBaseFolder) {
		this(serializedBaseFolder, null);
	}

	public SerializedStructDefinitionService(String serializedBaseFolder, String filePrefix) {
		structDescriptorValues = new HashMap<>();
		arrayDescriptorValues = new HashMap<>();
		folder = new File(serializedBaseFolder);
		this.filePrefix = filePrefix;
		if (!folder.exists() && !folder.mkdirs()) {
			throw new OsmConnectorException("Cant create Oracle serialization folder " + folder.getAbsolutePath());
		}
		if (!folder.canRead()) {
			throw new OsmConnectorException("Cant read Oracle serialization folder " + folder.getAbsolutePath());
		}
	}

	/* (non-Javadoc)
	 * @see org.lab.osm.connector.mapper.StructDefinitionService#structDescriptor(java.lang.String, java.sql.Connection)
	 */
	@Override
	public StructDescriptor structDescriptor(@NonNull String typeName, Connection conn) {
		try {
			if (structDescriptorValues.containsKey(typeName)) {
				// TODO check reusing connections
				return structDescriptorValues.get(typeName);
			}
			StructDescriptor structDescriptor;
			File file = getSerializedFile(typeName);
			if (file.exists()) {
				log.info("Reading strucy {} descriptor from file", typeName);
				try (FileInputStream fileInputStream = new FileInputStream(file)) {
					ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
					structDescriptor = (StructDescriptor) objectInputStream.readObject();
					objectInputStream.close();
				}
				structDescriptor.setConnection(conn);
			}
			else {
				log.info("Reading strucy {} descriptor from database", typeName);
				structDescriptor = new StructDescriptor(typeName, conn);
				try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
					ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
					objectOutputStream.writeObject(structDescriptor);
					objectOutputStream.close();
				}
			}
			structDescriptorValues.put(typeName, structDescriptor);
			return structDescriptor;
		}
		catch (Exception ex) {
			throw new OsmConnectorException("Error reading struct descriptor " + typeName, ex);
		}
	}

	@Override
	public ArrayDescriptor arrayDescriptor(@NonNull String typeName, Connection conn) {
		try {
			if (arrayDescriptorValues.containsKey(typeName)) {
				// TODO check reusing connections
				return arrayDescriptorValues.get(typeName);
			}
			ArrayDescriptor arrayDescriptor;
			File file = getSerializedFile(typeName);
			if (file.exists()) {
				log.info("Reading array {} descriptor from file", typeName);
				try (FileInputStream fileInputStream = new FileInputStream(file)) {
					ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
					arrayDescriptor = (ArrayDescriptor) objectInputStream.readObject();
					objectInputStream.close();
				}
				arrayDescriptor.setConnection(conn);
			}
			else {
				log.info("Reading array {} descriptor from database", typeName);
				arrayDescriptor = new ArrayDescriptor(typeName, conn);
				try (FileOutputStream fileOutStream = new FileOutputStream(file)) {
					ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
					objectOutStream.writeObject(arrayDescriptor);
					objectOutStream.close();
				}
			}
			arrayDescriptorValues.put(typeName, arrayDescriptor);
			return arrayDescriptor;
		}
		catch (Exception ex) {
			throw new OsmConnectorException("Error reading array descriptor " + typeName, ex);
		}
	}

	// TODO posibilidad de parametrizar la obtencion del nombre
	private File getSerializedFile(String typeName) {
		StringBuilder fileName = new StringBuilder();
		if (StringUtils.isNotBlank(filePrefix)) {
			fileName.append(filePrefix).append("-");
		}
		fileName.append(typeName).append(FILE_EXT);
		return new File(folder, fileName.toString());
	}

}
