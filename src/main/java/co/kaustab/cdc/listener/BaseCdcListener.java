package co.kaustab.cdc.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import co.kaustab.cdc.model.DebeziumPipeline;
import co.kaustab.cdc.service.sink.SinkService;
import co.kaustab.cdc.utils.Operation;
import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author kaustab
 * @reference https://debezium.io/documentation/reference/stable/development/engine.html
 * @description Your application needs to set up an embedded engine for each
 *              connector instance you want to run
 */
@Slf4j
public abstract class BaseCdcListener {

	protected final Executor executor = Executors.newSingleThreadExecutor();
	protected final Executor monitor = Executors.newSingleThreadExecutor();
	
	protected final EmbeddedEngine engine;
	protected final Map<String, SinkService> sinkServiceMap;
	protected final DebeziumPipeline debeziumPipeline;

	protected BaseCdcListener(DebeziumPipeline debeziumPipeline, Configuration configuration,
			Map<String, SinkService> sinkServiceMap) {
		this.debeziumPipeline = debeziumPipeline;
		this.engine = EmbeddedEngine.create().using(configuration).notifying(this::handleCdcEvent).build();
		this.sinkServiceMap = sinkServiceMap;
	}

	// executes whenever a CDC event is received
	protected abstract void handleCdcEvent(SourceRecord sourceRecord);

	@PostConstruct
	private void start() {
		this.executor.execute(engine);
		this.monitor.execute(new Ping(engine));
	}

	static class Ping implements Runnable {
		
		EmbeddedEngine engine;
		
		Ping(EmbeddedEngine engine) {
			this.engine = engine;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("engine running - "+this.engine.isRunning());
		}
	}

	@PreDestroy
	private void stop() {
		if (this.engine != null) {
			this.engine.stop();
		}
	}

	public final Map<String, Object> getOldRecord(SourceRecord sourceRecord) {
		Struct sourceRecordValue = (Struct) sourceRecord.value();
		Struct struct = (Struct) sourceRecordValue.get(io.debezium.data.Envelope.FieldName.BEFORE);
		return resolveMessages(struct);
	}

	public final Map<String, Object> getNewRecord(SourceRecord sourceRecord) {
		Struct sourceRecordValue = (Struct) sourceRecord.value();
		Struct struct = (Struct) sourceRecordValue.get(io.debezium.data.Envelope.FieldName.AFTER);
		return resolveMessages(struct);
	}

	public final Map<String, Object> resolveMessages(Struct struct) {
		Map<String, Object> message = new HashMap<>();
		if (struct != null && struct.schema() != null && struct.schema().fields() != null) {
			message = struct.schema().fields().stream().map(Field::name)
					.filter(fieldName -> struct.get(fieldName) != null)
					.map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
					.collect(HashMap::new, (m,v)->m.put(v.getKey(), v.getValue()), HashMap::putAll);
		}
		return message;
	}

	public final String getFormattedSource(SourceRecord sourceRecord) {
		return sourceRecord.valueSchema().name().split("\\.")[1] + "."
				+ sourceRecord.valueSchema().name().split("\\.")[2];
	}

	public final Operation getOperationType(SourceRecord sourceRecord) {
		Struct sourceRecordValue = (Struct) sourceRecord.value();
		String operationName = (String) sourceRecordValue.get(io.debezium.data.Envelope.FieldName.OPERATION);
		Operation operation = Operation.forCode(operationName);
		return operation;
	}
}
