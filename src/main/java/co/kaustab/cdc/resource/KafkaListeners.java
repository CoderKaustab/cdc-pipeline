package co.kaustab.cdc.resource;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

	@KafkaListener(topics = { "offset-storage-topic" }, groupId = "spring-boot-kafka")
	public void consume(String quote) {
		System.out.println("received= " + quote);
		ConnectorOffsetTailInfo.add("mysql-rds-connector-v4", quote);
		System.out.println("LIVE INFO ----> "+ConnectorOffsetTailInfo.get("mysql-rds-connector-v4"));
	}
}
