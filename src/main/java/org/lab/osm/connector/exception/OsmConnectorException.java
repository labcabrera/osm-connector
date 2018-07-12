package org.lab.osm.connector.exception;

/**
 * Base connector exception.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class OsmConnectorException extends RuntimeException {

	/**
	 * Public constructor.
	 * 
	 * @param message
	 * @param throwable
	 */
	public OsmConnectorException(String message, Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * Public constructor.
	 * 
	 * @param message
	 */
	public OsmConnectorException(String message) {
		super(message);
	}

	/**
	 * Public constructor.
	 * 
	 * @param throwable
	 */
	public OsmConnectorException(Throwable throwable) {
		super(throwable);
	}

}
