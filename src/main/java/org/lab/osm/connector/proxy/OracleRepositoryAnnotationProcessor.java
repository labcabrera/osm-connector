package org.lab.osm.connector.proxy;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.lab.osm.connector.annotation.OracleStoredProcedure;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OracleRepositoryAnnotationProcessor implements BeanFactoryPostProcessor, InitializingBean {

	@Getter
	@Setter
	private String basePackage;

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		log.debug("Processing beans");

		DefaultListableBeanFactory registry = (DefaultListableBeanFactory) beanFactory;

		Set<Class<?>> repositories = new Reflections(basePackage).getTypesAnnotatedWith(OracleStoredProcedure.class);

		for (Class<?> clazz : repositories) {

			MethodCallback mc = new MethodCallback() {

				@Override
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
					log.debug("TODO doWith");
				}
			};

			ReflectionUtils.doWithMethods(clazz, mc);

			AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder //@formatter:off
				.genericBeanDefinition(OracleRepositoryInvocationHandler.class)
				.addConstructorArgValue(clazz)
				.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
				.getBeanDefinition(); //@formatter:on

			registry.registerBeanDefinition(clazz.getName() + "OsmProxy", beanDefinition);

		}

	}

}