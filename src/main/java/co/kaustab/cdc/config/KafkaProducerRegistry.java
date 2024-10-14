package co.kaustab.cdc.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import co.kaustab.cdc.model.PipelineSink;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaProducerRegistry implements SinkConfigRegistry {

	private static List<String> registeredKafkaProducers = new ArrayList<>();

	/**
	 * The method registers one kafka producer per Kafka server that can be used to
	 * produce messages to all the topics created in that kafka server
	 */
	@Override
	public void register(ConfigurableApplicationContext context, PipelineSink pipelineSink) throws Exception {

		// check if producer has been already created or not for this kafka server
		Map<String, String> producerConfigs = pipelineSink.getPipelineSinkConfig().getProducerConfig();
		String kafkaProducerName = getKafkaProducerName(producerConfigs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
		if (registeredKafkaProducers.contains(kafkaProducerName)) {
			return;
		}

		// build kafka properties
		Map<String, Object> producerProperties = new HashMap<>();
		for (Entry<String, String> entry : producerConfigs.entrySet()) {
			if (entry.getKey().equals(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)
					|| entry.getKey().equals(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
				Class clazz = Class.forName(entry.getValue());
				producerProperties.put(entry.getKey(), clazz);
			} else {
				producerProperties.put(entry.getKey(), entry.getValue());
			}
		}

		// register kafka producer
		GenericBeanDefinition kafkaProducerGenericBeanDefinition = new GenericBeanDefinition();
		kafkaProducerGenericBeanDefinition.setBeanClass(KafkaProducer.class);
		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addGenericArgumentValue(producerProperties);
		kafkaProducerGenericBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);

		((GenericWebApplicationContext) context).registerBeanDefinition(kafkaProducerName,
				kafkaProducerGenericBeanDefinition);
		log.info("context registered bean - kafka producer with name - " + kafkaProducerName + " ===> "
				+ context.getBean(kafkaProducerName));

		registeredKafkaProducers.add(kafkaProducerName);

	}

	public static String getKafkaProducerName(String bootstrapServer) {
		return "KafkaProducer{" + bootstrapServer + "}";
	}

}
