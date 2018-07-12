package org.lab.osm.connector.exception;

/**
 * Connector mapping exception.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class OsmMappingException extends OsmConnectorException {

	/**
	 * Public constructor.
	 * 
	 * @param message
	 * @param throwable
	 */
	public OsmMappingException(String message, Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * Public constructor.
	 * 
	 * @param message
	 */
	public OsmMappingException(String message) {
		super(message);
	}

	/**
	 * Public constructor.
	 * 
	 * @param throwable
	 */
	public OsmMappingException(Throwable throwable) {
		super(throwable);
	}

}
