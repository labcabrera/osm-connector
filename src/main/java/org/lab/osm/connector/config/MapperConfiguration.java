package org.lab.osm.connector.config;

import org.lab.osm.connector.mapper.service.AnnotationStructMapperService;
import org.lab.osm.connector.mapper.service.CustomStructMapperService;
import org.lab.osm.connector.mapper.service.StructMapperService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfiguration {

	@Bean("mapper-annotation")
	StructMapperService mapperServiceAnnotation() {
		return new AnnotationStructMapperService();
	}

	@Bean("mapper-custom")
	StructMapperService mapperServiceCustom() {
		return new CustomStructMapperService();
	}

}
