package org.lab.osm.connector.handler;

import javax.sql.DataSource;

import org.springframework.jdbc.object.StoredProcedure;

/**
 * Spring JDBC <code>StoredProcedure</code> implementation.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
public class DelegateStoredProcedure extends StoredProcedure {

	/**
	 * Public constructor.
	 * 
	 * @param ds
	 * @param name
	 */
	public DelegateStoredProcedure(DataSource ds, String name) {
		super(ds, name);
	}

}
