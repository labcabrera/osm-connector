package org.lab.osm.connector.mapper.service;

import org.springframework.data.jdbc.support.oracle.StructMapper;

public interface StructMapperService {

	<T> StructMapper<T> mapper(Class<T> mappedClass);
}
