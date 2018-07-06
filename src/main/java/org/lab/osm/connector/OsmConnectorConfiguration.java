package org.lab.osm.connector;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.lab.osm.connector.exception.OsmConnectorException;
import org.lab.osm.connector.handler.OracleStoredProcedureAnnotationProcessor;
import org.lab.osm.connector.mapper.StructDefinitionService;
import org.lab.osm.connector.metadata.DefaultMetadataCollector;
import org.lab.osm.connector.metadata.MetadataCollector;
import org.lab.osm.connector.service.MetadataStructMapperService;
import org.lab.osm.connector.service.StructMapperService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring configuration.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class OsmConnectorConfiguration implements ImportBeanDefinitionRegistrar {

	private static final String MSG_NEW_BEAN_DEFINITION = "No {} has been defined. Creating default implementation";

	/* (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes attributes = AnnotationAttributes
			.fromMap(importingClassMetadata.getAnnotationAttributes(EnableOsmConnector.class.getName(), false));

		String[] modelPackages = attributes.getStringArray("modelPackages");
		String[] executorPackages = attributes.getStringArray("executorPackages");
		String dataBaseName = attributes.getString("dataBaseName");

		log.info("Configuring OSM connector. Model packages {}, procedure packages: {}", modelPackages,
			executorPackages);

		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
		processOracleRepositoryAnnotationProcessor(beanFactory, executorPackages);
		processMetadataCollector(beanFactory, dataBaseName);
		processMetadataStructMapperService(beanFactory, modelPackages, dataBaseName);
		processDefaultStructDefinitionService(beanFactory);
	}

	private void processOracleRepositoryAnnotationProcessor(DefaultListableBeanFactory beanFactory,
		String[] executorPackages) {
		String[] names = beanFactory.getBeanNamesForType(OracleStoredProcedureAnnotationProcessor.class);
		if (names.length > 0) {
			return;
		}
		log.debug(MSG_NEW_BEAN_DEFINITION, DefaultListableBeanFactory.class.getName());
		String beanName = getBeanName(OracleStoredProcedureAnnotationProcessor.class);
		BeanDefinition beanDef = BeanDefinitionBuilder // @formatter:off
			.genericBeanDefinition(OracleStoredProcedureAnnotationProcessor.class)
			.addPropertyValue("basePackages", executorPackages)
			.getBeanDefinition(); //@formatter:on
		beanFactory.registerBeanDefinition(beanName, beanDef);
	}

	private void processMetadataStructMapperService(DefaultListableBeanFactory beanFactory, String[] modelPackages,
		String customDataSourceBeanName) {
		String[] names = beanFactory.getBeanNamesForType(StructMapperService.class);
		if (names.length > 0) {
			return;
		}
		log.debug(MSG_NEW_BEAN_DEFINITION, StructMapperService.class.getName());

		String definitionServiceBeanName = getBeanName(beanFactory, StructDefinitionService.class);
		String metadataCollectorBeanName = getBeanName(beanFactory, MetadataCollector.class);
		String dataSourceBeanName = resolveDataSourceName(beanFactory, customDataSourceBeanName);
		String beanName = getBeanName(MetadataStructMapperService.class);

		BeanDefinition beanDef = BeanDefinitionBuilder // @formatter:off
			.genericBeanDefinition(MetadataStructMapperService.class)
			.addConstructorArgReference(dataSourceBeanName)
			.addConstructorArgReference(definitionServiceBeanName)
			.addConstructorArgReference(metadataCollectorBeanName)
			.addConstructorArgValue(modelPackages)
			.getBeanDefinition(); //@formatter:on
		beanFactory.registerBeanDefinition(beanName, beanDef);
	}

	private void processMetadataCollector(DefaultListableBeanFactory beanFactory, String customDataSourceBeanName) {
		String[] names = beanFactory.getBeanNamesForType(MetadataCollector.class);
		if (names.length > 0) {
			return;
		}
		log.debug(MSG_NEW_BEAN_DEFINITION, MetadataCollector.class.getName());
		String dataSourceName = resolveDataSourceName(beanFactory, customDataSourceBeanName);
		// TODO Json metadata collector impl
		// String objectMapperBeanName = getBeanName(beanFactory, ObjectMapper.class);
		String beanName = getBeanName(MetadataCollector.class);
		BeanDefinition beanDef = BeanDefinitionBuilder // @formatter:off
			.genericBeanDefinition(DefaultMetadataCollector.class)
			.addConstructorArgReference(dataSourceName)
			.getBeanDefinition(); //@formatter:on
		beanFactory.registerBeanDefinition(beanName, beanDef);
	}

	private void processDefaultStructDefinitionService(DefaultListableBeanFactory beanFactory) {
		String[] names = beanFactory.getBeanNamesForType(StructDefinitionService.class);
		if (names.length > 0) {
			return;
		}
		log.debug(MSG_NEW_BEAN_DEFINITION, StructDefinitionService.class);
		// TODO Json metadata collector impl
		String beanName = getBeanName(MetadataCollector.class);
		BeanDefinition beanDef = BeanDefinitionBuilder // @formatter:off
			.genericBeanDefinition(StructDefinitionService.class)
			.getBeanDefinition(); //@formatter:on
		beanFactory.registerBeanDefinition(beanName, beanDef);
	}

	private String resolveDataSourceName(DefaultListableBeanFactory beanFactory, String customDataSourceBeanName) {
		return StringUtils.isBlank(customDataSourceBeanName) ? getBeanName(beanFactory, DataSource.class)
			: customDataSourceBeanName;
	}

	private String getBeanName(Class<?> type) {
		return StringUtils.uncapitalize(type.getSimpleName());
	}

	private String getBeanName(@NonNull DefaultListableBeanFactory beanFactory, @NonNull Class<?> type) {
		String[] names = beanFactory.getBeanNamesForType(type);
		if (names.length == 0) {
			throw new OsmConnectorException("Undefined bean definition for class " + type.getName());
		}
		else if (names.length > 1) {
			throw new OsmConnectorException("Multiple bean definitions for class " + type.getName());
		}
		return names[0];
	}

}
