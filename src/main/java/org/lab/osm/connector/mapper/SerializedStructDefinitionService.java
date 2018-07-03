package org.lab.osm.connector.mapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.lab.osm.connector.exception.OsmConnectorException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import oracle.sql.ArrayDescriptor;
import oracle.sql.StructDescriptor;

@Slf4j
public class SerializedStructDefinitionService implements StructDefinitionService {

	private String FILE_EXT = ".ser";

	private final File folder;
	private final Map<String, StructDescriptor> structDescriptorValues;
	private final Map<String, ArrayDescriptor> arrayDescriptorValues;

	public SerializedStructDefinitionService(String serializedBaseFolder) {
		structDescriptorValues = new HashMap<>();
		arrayDescriptorValues = new HashMap<>();
		folder = new File(serializedBaseFolder);
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
				return structDescriptorValues.get(typeName);
			}
			else {
				StructDescriptor desc;
				File file = getSerializedFile(typeName);
				if (file.exists()) {
					log.info("Reading strucy {} descriptor from file", typeName);
					FileInputStream fis = new FileInputStream(file);
					ObjectInputStream ois = new ObjectInputStream(fis);
					desc = (StructDescriptor) ois.readObject();
					ois.close();
				}
				else {
					log.info("Reading strucy {} descriptor from database", typeName);
					desc = new StructDescriptor(typeName, conn);
					FileOutputStream fout = new FileOutputStream(file);
					ObjectOutputStream out = new ObjectOutputStream(fout);
					out.writeObject(desc);
					out.close();
				}
				structDescriptorValues.put(typeName, desc);
				return desc;
			}
		}
		catch (Exception ex) {
			throw new OsmConnectorException("Error reading struct descriptor " + typeName, ex);
		}
	}

	@Override
	public ArrayDescriptor arrayDescriptor(@NonNull String typeName, Connection conn) {
		try {
			if (arrayDescriptorValues.containsKey(typeName)) {
				return arrayDescriptorValues.get(typeName);
			}
			else {
				log.info("Reading array {} descriptor", typeName);
				ArrayDescriptor desc = new ArrayDescriptor(typeName, conn);
				arrayDescriptorValues.put(typeName, desc);
				return desc;
			}
		}
		catch (Exception ex) {
			throw new OsmConnectorException("Error reading array descriptor " + typeName, ex);
		}
	}

	// TODO posibilidad de parametrizar la obtencion del nombre
	private File getSerializedFile(String typeName) {
		return new File(folder, typeName + FILE_EXT);
	}

}
