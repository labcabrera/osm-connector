package org.lab.soc.jdbc.mapper;

import org.lab.soc.jdbc.AnnotationStructMapper;
import org.lab.soc.jdbc.StructDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.support.oracle.StructMapper;

public class AnnotationStructMapperService implements StructMapperService {

	@Autowired
	private StructDefinitionService definitionService;

	@Override
	public <T> StructMapper<T> mapper(Class<T> mappedClass) {
		return new AnnotationStructMapper<>(mappedClass, definitionService);
	}

}
