package co.kaustab.cdc.resource;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaListeners {

	public static Map<String, String> monitorTopicConnector = new HashMap<>();

	private final String MONITOR_TOPIC = "offset-storage-topic";

	@KafkaListener(topics = { MONITOR_TOPIC }, groupId = "spring-boot-kafka")
	public void consume(String quote) {
		
		log.info("received= " + quote);
		
		String connectorName = monitorTopicConnector.get(MONITOR_TOPIC);
		
		ConnectorOffsetTailInfo.add(connectorName, quote);
	}

}
