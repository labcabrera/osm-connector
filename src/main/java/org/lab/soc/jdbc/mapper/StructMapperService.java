package org.lab.soc.jdbc.mapper;

import org.springframework.data.jdbc.support.oracle.StructMapper;

public interface StructMapperService {

	<T> StructMapper<T> mapper(Class<T> mappedClass);
}
