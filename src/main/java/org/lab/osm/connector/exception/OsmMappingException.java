package org.lab.osm.connector.exception;

@SuppressWarnings("serial")
public class OsmMappingException extends OsmConnectorException {

	public OsmMappingException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public OsmMappingException(String message) {
		super(message);
	}

	public OsmMappingException(Throwable throwable) {
		super(throwable);
	}

}
