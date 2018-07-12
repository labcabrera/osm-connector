package org.lab.osm.connector.mapper;

/**
 * Interface used to obtain the StructMapper associated with a given entity.
 *
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 * 
 * @see StructMapper
 * @see ArrayMapper
 */
public interface StructMapperService {

	/**
	 * Gets the {@link StructMapper} used to perform conversions between Oracle STRUCTs and model entities.
	 * 
	 * @param mappedClass Entity class
	 * @return StructMapper
	 */
	<T> StructMapper<T> mapper(Class<T> mappedClass);

	/**
	 * Gets the {@link ArrayMapper} used to perform conversions between Oracle ARRAYs and entity lists.
	 * @param mappedClass
	 * @param collectionName
	 * @return
	 */
	<T> ArrayMapper<T> arrayMapper(Class<T> mappedClass, String collectionName);
}
