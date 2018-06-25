package org.lab.osm.connector.metadata.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleMappingData {

	private final List<OracleMappingStructData> structs;

	private String packageName;

	public OracleMappingData() {
		structs = new ArrayList<>();
	}

	public void register(OracleMappingStructData data) {
		structs.add(data);
	}

}
