package org.lab.osm.connector.service;

import org.lab.osm.connector.mapper.ArrayMapper;
import org.lab.osm.connector.mapper.StructMapper;

/**
 * Interface used to obtain the StructMapper associated with a given entity.
 *
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
public interface StructMapperService {

	/**
	 * Gets the StructMapper used to do the type conversions of an mapped entity.
	 * 
	 * @param mappedClass Entity class
	 * @return StructMapper
	 */
	<T> StructMapper<T> mapper(Class<T> mappedClass);

	<T> ArrayMapper<T> arrayMapper(Class<T> mappedClass, String collectionName);
}
