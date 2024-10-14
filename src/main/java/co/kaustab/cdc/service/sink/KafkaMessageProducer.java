package co.kaustab.cdc.service.sink;

import java.util.Arrays;
import java.util.List;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import co.kaustab.cdc.config.KafkaProducerRegistry;
import co.kaustab.cdc.message.builder.MessageBuilder;
import co.kaustab.cdc.model.BaseEventModel;
import co.kaustab.cdc.model.DebeziumPipeline;
import co.kaustab.cdc.model.MessageSinkModel;
import co.kaustab.cdc.model.PipelineSinkConfig;
import co.kaustab.cdc.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = KafkaMessageProducer.SERVICE_NAME)
@SuppressWarnings("unchecked")
public class KafkaMessageProducer implements SinkService {

	public static final String SERVICE_NAME = "kafkaMessagingService";

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private JsonUtils jsonUtils;

	@Override
	public void sink(MessageSinkModel messageSinkModel) throws Exception {

		DebeziumPipeline debeziumPipeline = messageSinkModel.getDebeziumPipeline();
		PipelineSinkConfig pipelineSinkConfig = SinkService.getPipelineSinkConfig(debeziumPipeline,
				messageSinkModel.getSinkName());

		// produce messages for white listed tables
		String whiteListedTables = pipelineSinkConfig.getWhitelistedTables();
		if (whiteListedTables != null) {
			List<String> whiteListedTableList = whiteListedTables.contains(",")
					? Arrays.asList(whiteListedTables.split(","))
					: Arrays.asList(whiteListedTables);
			if (!whiteListedTableList.contains(messageSinkModel.getSource())) {
				return;
			}
		}

		String kafkaProducerName = KafkaProducerRegistry.getKafkaProducerName(
				pipelineSinkConfig.getProducerConfig().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
		KafkaProducer<String, String> kafkaProducer = (KafkaProducer<String, String>) applicationContext
				.getBean(kafkaProducerName);

		// build kafka message
		pipelineSinkConfig.getMessageSettings().forEach(messageSettings -> {
			String messageSources = messageSettings.getMessageSources();
			List<String> messageSourceList = messageSources.contains(",") ? Arrays.asList(messageSources.split(","))
					: Arrays.asList(messageSources);

			if (messageSourceList.contains(messageSinkModel.getSource())) {
				messageSettings.getKafkaTopicMessageSettings().forEach(topicMessageSettings -> {
					String messageBuilderServiceName = topicMessageSettings.getMessageBuilderComponent();
					MessageBuilder messageBuilderService = (MessageBuilder) applicationContext
							.getBean(messageBuilderServiceName);
					BaseEventModel eventModel = messageBuilderService.build(messageSinkModel);

					List<String> topics = topicMessageSettings.getTopics().contains(",")
							? Arrays.asList(topicMessageSettings.getTopics().split(","))
							: Arrays.asList(topicMessageSettings.getTopics());
					topics.forEach(topic -> {
						sendMessage(kafkaProducer, topic, eventModel);
					});
				});
			}
		});
	}

	public void sendMessage(KafkaProducer<String, String> kafkaProducer, String topic, BaseEventModel eventModel) {
		if (eventModel == null) {
			return;
		}
		String jsonData = jsonUtils.convertToJSON(eventModel);
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, jsonData);

		kafkaProducer.send(record, new Callback() {
			@Override
			public void onCompletion(RecordMetadata metadata, Exception e) {
				if (e != null) {
					e.printStackTrace();
				} else {
					log.info("message produced to kafka topic: " + topic + ", data: " + jsonData);
				}
			}
		});
	}

}
