package co.kaustab.cdc.model;

import java.util.Map;

import org.apache.kafka.connect.source.SourceRecord;

import co.kaustab.cdc.utils.Operation;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageSinkModel {

	private SourceRecord sourceRecord;
	private String source;
	private String sinkName;
	private Operation operation;
	private Map<String, Object> oldMessages;
	private Map<String, Object> newMessages;
	private DebeziumPipeline debeziumPipeline;

}
