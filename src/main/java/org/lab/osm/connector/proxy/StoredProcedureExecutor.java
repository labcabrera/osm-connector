package org.lab.osm.connector.proxy;

import java.util.Map;

public interface StoredProcedureExecutor {

	Map<String, Object> execute(Object... args);

}
