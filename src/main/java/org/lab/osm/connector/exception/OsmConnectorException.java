package org.lab.osm.connector.exception;

@SuppressWarnings("serial")
public class OsmConnectorException extends RuntimeException {

	public OsmConnectorException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public OsmConnectorException(String message) {
		super(message);
	}

	public OsmConnectorException(Throwable throwable) {
		super(throwable);
	}

}
