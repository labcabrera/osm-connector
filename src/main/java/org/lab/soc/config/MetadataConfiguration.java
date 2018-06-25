package org.lab.soc.config;

import javax.sql.DataSource;

import org.lab.soc.jdbc.metadata.MetadataCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetadataConfiguration {

	// @ConditionalOnMissingBean(MetadataCollector.class)
	@Bean
	MetadataCollector metadataCollector(DataSource dataSource) {
		return new MetadataCollector(dataSource);
	}

}
