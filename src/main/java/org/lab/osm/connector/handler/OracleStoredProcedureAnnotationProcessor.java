package org.lab.osm.connector.handler;

import java.util.Set;

import org.lab.osm.connector.annotation.OracleStoredProcedure;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.Assert;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <code>BeanFactoryPostProcessor</code> to register {@link StoredProcedureInvocationHandler} from a given sets of base
 * packages.
 *
 * @author lab.cabrera@gmail.com
 * @since 1.0.0
 */
@Slf4j
public class OracleStoredProcedureAnnotationProcessor implements BeanFactoryPostProcessor {

	@Setter
	private String[] basePackages;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		log.debug("Processing Oracle stored procedure invocation handlers");
		Assert.isTrue(basePackages != null && basePackages.length > 0, "Required base packages");
		DefaultListableBeanFactory registry = (DefaultListableBeanFactory) beanFactory;
		for (String basePackage : basePackages) {
			log.debug("Scanning package '{}'", basePackage);
			postProcessPackage(basePackage, registry);
		}
	}

	private void postProcessPackage(String basePackage, DefaultListableBeanFactory registry) {
		Set<Class<?>> repositories = new Reflections(basePackage).getTypesAnnotatedWith(OracleStoredProcedure.class);
		for (Class<?> clazz : repositories) {
			String beanName = clazz.getSimpleName() + "OsmInvocationHandler";
			log.debug("Registering handler '{}' as '{}'", clazz.getSimpleName(), beanName);

			AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder //@formatter:off
				.genericBeanDefinition(StoredProcedureInvocationHandler.class)
				.addConstructorArgValue(clazz)
				.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
				.getBeanDefinition(); //@formatter:on

			registry.registerBeanDefinition(beanName, beanDefinition);
		}
	}

}