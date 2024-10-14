package co.kaustab.cdc.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import co.kaustab.cdc.model.CustomDebeziumConfiguration;
import co.kaustab.cdc.model.DebeziumPipeline;
import co.kaustab.cdc.model.DebeziumSourceConnectorConfiguration;
import co.kaustab.cdc.service.sink.SinkService;
import co.kaustab.cdc.utils.JasyptUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CdcListenerRegistrationConfig {

	/**
	 * The method registers debezium connector in the Spring context of class type -
	 * DebeziumPipeline.pipelineSource.clazz that reads all the bin logs from the database
	 */
	@SuppressWarnings("unchecked")
	public static void initCdcListener(ConfigurableApplicationContext context, DebeziumPipeline debeziumPipeline)
			throws Exception {

		DebeziumSourceConnectorConfiguration sourceConnectorConfig = debeziumPipeline.getPipelineSource()
				.getDebeziumSourceConnectorConfiguration();

		String sourceConnectorName = sourceConnectorConfig.getConnectorConfigs().get("name");

		JasyptUtils jasyptUtils = (JasyptUtils) context.getBean("jasyptUtils");

		// register connector config
		Map<String, Object> configProperties = new HashMap<>(sourceConnectorConfig.getConnectorConfigs());
		configProperties.put("name", sourceConnectorName);
		configProperties.put("database.password",
				jasyptUtils.decrypt((String) configProperties.get("database.password")));
		log.info("Registering Mysql-Redis-connector :::: configProperties ----> " + configProperties);

		GenericBeanDefinition connectorConfigGenericBeanDefinition = new GenericBeanDefinition();
		connectorConfigGenericBeanDefinition.setBeanClass(CustomDebeziumConfiguration.class);
		MutablePropertyValues mpv = new MutablePropertyValues();
		Properties properties = new Properties();
		properties.putAll(configProperties);
		mpv.add("props", properties);
		connectorConfigGenericBeanDefinition.setPropertyValues(mpv);
		String connectorConfigName = getDebeziumSourceConnectorConfigBeanName(sourceConnectorConfig.getConnectorName());
		((GenericWebApplicationContext) context).registerBeanDefinition(connectorConfigName,
				connectorConfigGenericBeanDefinition);
		log.info("context registered bean - CustomDebeziumConfiguration ===> " + context.getBean(connectorConfigName));

		// register CDC listener
		CustomDebeziumConfiguration debeziumConfiguration = (CustomDebeziumConfiguration) context
				.getBean(getDebeziumSourceConnectorConfigBeanName(sourceConnectorName));
		Map<String, SinkService> sinkServiceMap = (Map<String, SinkService>) debeziumPipeline.getPipelineSinks()
				.stream().collect(Collectors.toMap(pipelineSink -> pipelineSink.getName(),
						pipelineSink -> (SinkService) context.getBean(pipelineSink.getServiceBeanName())));
		GenericBeanDefinition cdcListenerGenericBeanDefinition = new GenericBeanDefinition();
		cdcListenerGenericBeanDefinition.setBeanClass(Class.forName(debeziumPipeline.getPipelineSource().getClazz()));
		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addGenericArgumentValue(debeziumPipeline);
		constructorArgumentValues.addGenericArgumentValue(debeziumConfiguration);
		constructorArgumentValues.addGenericArgumentValue(sinkServiceMap);
		constructorArgumentValues.addGenericArgumentValue(false);
		cdcListenerGenericBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);
		((GenericWebApplicationContext) context).registerBeanDefinition(sourceConnectorName,
				cdcListenerGenericBeanDefinition);
		log.info("sourceConnectorName bean: " + sourceConnectorName + " from context ---> "
				+ context.getBean(sourceConnectorName)); // required for invoking constructor
	}

	public static String getDebeziumSourceConnectorConfigBeanName(String connectorName) {
		return connectorName + "_config";
	}

}
