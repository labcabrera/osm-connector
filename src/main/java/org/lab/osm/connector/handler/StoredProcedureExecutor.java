package org.lab.osm.connector.handler;

import java.util.Map;

public interface StoredProcedureExecutor {

	Map<String, Object> execute(Object... args);

}
