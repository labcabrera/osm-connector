package org.lab.osm.connector;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.lab.osm.connector.exception.OsmConnectorException;
import org.lab.osm.connector.handler.OracleStoredProcedureAnnotationProcessor;
import org.lab.osm.connector.mapper.DefaultStructDefinitionService;
import org.lab.osm.connector.mapper.SerializedStructDefinitionService;
import org.lab.osm.connector.mapper.StructDefinitionService;
import org.lab.osm.connector.metadata.DefaultMetadataCollector;
import org.lab.osm.connector.metadata.JsonMetadataCollector;
import org.lab.osm.connector.metadata.MetadataCollector;
import org.lab.osm.connector.service.MetadataStructMapperService;
import org.lab.osm.connector.service.StructMapperService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	private static final String MSG_NEW_BEAN_DEFINITION = "No bean '{}' has been defined. Creating default implementation";

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
		String serializationFolder = attributes.getString("serializationFolder");
		String serializationPrefix = attributes.getString("serializationPrefix");

		log.info("Configuring OSM connector. Model packages {}, procedure packages: {}", modelPackages,
			executorPackages);

		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
		processOracleRepositoryAnnotationProcessor(beanFactory, executorPackages);
		processMetadataCollector(beanFactory, dataBaseName, serializationFolder, serializationPrefix);
		processStructDefinitionService(beanFactory, serializationFolder, serializationPrefix);
		processMetadataStructMapperService(beanFactory, modelPackages, dataBaseName);
	}

	private void processOracleRepositoryAnnotationProcessor(DefaultListableBeanFactory beanFactory,
		String[] executorPackages) {
		String[] names = beanFactory.getBeanNamesForType(OracleStoredProcedureAnnotationProcessor.class);
		if (names.length > 0) {
			return;
		}
		log.debug(MSG_NEW_BEAN_DEFINITION, OracleStoredProcedureAnnotationProcessor.class.getSimpleName());
		String beanName = getBeanName(OracleStoredProcedureAnnotationProcessor.class);
		BeanDefinition beanDefinition = BeanDefinitionBuilder // @formatter:off
			.genericBeanDefinition(OracleStoredProcedureAnnotationProcessor.class)
			.addPropertyValue("basePackages", executorPackages)
			.getBeanDefinition(); //@formatter:on
		beanFactory.registerBeanDefinition(beanName, beanDefinition);
	}

	private void processMetadataCollector(DefaultListableBeanFactory beanFactory, String customDataSourceBeanName,
		String serializationFolder, String serializationPrefix) {
		String[] names = beanFactory.getBeanNamesForType(MetadataCollector.class);
		if (names.length > 0) {
			return;
		}
		log.debug(MSG_NEW_BEAN_DEFINITION, MetadataCollector.class.getSimpleName());
		String dataSourceName = resolveDataSourceName(beanFactory, customDataSourceBeanName);
		BeanDefinition beanDefinition;
		String beanName = getBeanName(MetadataCollector.class);
		if (StringUtils.isBlank(serializationFolder)) {
			beanDefinition = BeanDefinitionBuilder // @formatter:off
				.genericBeanDefinition(DefaultMetadataCollector.class)
				.addConstructorArgReference(dataSourceName)
				.getBeanDefinition(); //@formatter:on
		}
		else {
			String objectMapperBeanName = getBeanName(beanFactory, ObjectMapper.class);
			beanDefinition = BeanDefinitionBuilder // @formatter:off
				.genericBeanDefinition(JsonMetadataCollector.class)
				.addConstructorArgReference(dataSourceName)
				.addConstructorArgReference(objectMapperBeanName)
				.addConstructorArgValue(serializationFolder)
				.addConstructorArgValue(serializationPrefix)
				.getBeanDefinition(); //@formatter:on
		}
		beanFactory.registerBeanDefinition(beanName, beanDefinition);
	}

	private void processMetadataStructMapperService(DefaultListableBeanFactory beanFactory, String[] modelPackages,
		String customDataSourceBeanName) {
		String[] names = beanFactory.getBeanNamesForType(StructMapperService.class);
		if (names.length > 0) {
			return;
		}
		log.debug(MSG_NEW_BEAN_DEFINITION, StructMapperService.class.getSimpleName());

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

	private void processStructDefinitionService(DefaultListableBeanFactory beanFactory, String serializationFolder,
		String serializationPrefix) {
		String[] names = beanFactory.getBeanNamesForType(StructDefinitionService.class);
		if (names.length > 0) {
			return;
		}
		log.debug(MSG_NEW_BEAN_DEFINITION, StructDefinitionService.class.getSimpleName());
		String beanName = getBeanName(StructDefinitionService.class);
		BeanDefinition beanDefinition;
		if (StringUtils.isBlank(serializationFolder)) {
			beanDefinition = BeanDefinitionBuilder // @formatter:off
				.genericBeanDefinition(DefaultStructDefinitionService.class)
				.getBeanDefinition(); //@formatter:on
		}
		else {
			beanDefinition = BeanDefinitionBuilder // @formatter:off
				.genericBeanDefinition(SerializedStructDefinitionService.class)
				.addConstructorArgValue(serializationFolder)
				.addConstructorArgValue(serializationPrefix)
				.getBeanDefinition(); //@formatter:on
		}
		beanFactory.registerBeanDefinition(beanName, beanDefinition);
	}

	private String resolveDataSourceName(DefaultListableBeanFactory beanFactory, String customDataSourceBeanName) {
		return StringUtils.isBlank(customDataSourceBeanName) ? getBeanName(beanFactory, DataSource.class)
			: customDataSourceBeanName;
	}

	private String getBeanName(Class<?> type) {
		return StringUtils.uncapitalize(type.getSimpleName());
	}

	private String getBeanName(@NonNull DefaultListableBeanFactory beanFactory, @NonNull Class<?> type) {

		ResolvableType rt = ResolvableType.forClass(type);

		beanFactory.getBeanNamesForType(rt);

		String[] names = beanFactory.getBeanNamesForType(rt);
		if (names.length == 0) {
			throw new OsmConnectorException("Undefined bean definition for class " + type.getName());
		}
		else if (names.length > 1) {
			throw new OsmConnectorException("Multiple bean definitions for class " + type.getName());
		}
		return names[0];
	}

}
