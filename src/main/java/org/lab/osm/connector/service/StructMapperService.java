package org.lab.osm.connector.service;

import org.lab.osm.connector.mapper.StructMapper;

public interface StructMapperService {

	<T> StructMapper<T> mapper(Class<T> mappedClass);
}
