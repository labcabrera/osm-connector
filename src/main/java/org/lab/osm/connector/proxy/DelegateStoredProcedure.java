package org.lab.osm.connector.proxy;

import javax.sql.DataSource;

import org.springframework.jdbc.object.StoredProcedure;

public class DelegateStoredProcedure extends StoredProcedure {

	public DelegateStoredProcedure(DataSource ds, String name) {
		super(ds, name);
	}

}
