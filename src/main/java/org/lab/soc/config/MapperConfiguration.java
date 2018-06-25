package org.lab.soc.config;

import org.lab.soc.jdbc.mapper.AnnotationStructMapperService;
import org.lab.soc.jdbc.mapper.CustomStructMapperService;
import org.lab.soc.jdbc.mapper.StructMapperService;
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
