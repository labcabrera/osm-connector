package org.lab.osm.connector.mapper;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.lab.osm.connector.exception.OsmConnectorException;

import lombok.extern.slf4j.Slf4j;
import oracle.sql.ArrayDescriptor;
import oracle.sql.StructDescriptor;

/**
 * Componente encargado de almacenar en memoria la definicion de las estructuras de datos de Oracle para que no sea
 * necesario una llamada a la base de datos cada vez que se tiene que realizar una transformacion de STRUCT a una
 * entidad o de una entidad a un STRUCT.
 * 
 */
// TODO estudiar estrategias para almacenar el descriptor: cache, ficheros , etc.
// TODO estudiar si se desea separar la parte de generacion de los ficheros de serializados o si es mejor generarlos una
// unica vez bajo demanda cuando se solicita por primera vez un tipo dado.
@Slf4j
public class StructDefinitionService {

	private final Map<String, StructDescriptor> structDescriptorValues;
	private final Map<String, ArrayDescriptor> arrayDescriptorValues;

	public StructDefinitionService() {
		structDescriptorValues = new HashMap<>();
		arrayDescriptorValues = new HashMap<>();
	}

	public StructDescriptor structDescriptor(String typeName, Connection conn) {
		try {
			if (structDescriptorValues.containsKey(typeName)) {
				return structDescriptorValues.get(typeName);
			}
			else {
				log.info("Reading strucy {} descriptor", typeName);
				StructDescriptor desc = new StructDescriptor(typeName, conn);
				structDescriptorValues.put(typeName, desc);
				return desc;
			}
		}
		catch (Exception ex) {
			throw new OsmConnectorException("Error reading struct descriptor " + typeName, ex);
		}
	}

	public ArrayDescriptor arrayDescriptor(String typeName, Connection conn) {
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

}
