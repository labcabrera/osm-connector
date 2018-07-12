package org.lab.osm.connector.handler;

import java.util.Map;

/**
 * Interface used to invoke stored procedures that returns output parameters on a map.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 * 
 * @see org.lab.osm.connector.annotation.OracleStoredProcedure
 */
public interface StoredProcedureExecutor {

	/**
	 * Execute a given Oracle stored procedure / function returning output parameters as a map.
	 * @param args
	 * @return
	 */
	Map<String, Object> execute(Object... args);

}
