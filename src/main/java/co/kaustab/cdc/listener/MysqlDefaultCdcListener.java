package co.kaustab.cdc.listener;

import java.util.Map;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import co.kaustab.cdc.model.CustomDebeziumConfiguration;
import co.kaustab.cdc.model.DebeziumPipeline;
import co.kaustab.cdc.model.MessageSinkModel;
import co.kaustab.cdc.service.sink.SinkService;
import co.kaustab.cdc.utils.Operation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlDefaultCdcListener extends BaseCdcListener {

	private boolean sinkDdl;

	private MysqlDefaultCdcListener(DebeziumPipeline debeziumPipeline, CustomDebeziumConfiguration configuration,
			Map<String, SinkService> sinkServiceMap, boolean sinkDdl) {
		super(debeziumPipeline, configuration, sinkServiceMap);
		this.sinkDdl = sinkDdl;
	}

	protected void handleCdcEvent(SourceRecord sourceRecord) {

		Struct sourceRecordValue = (Struct) sourceRecord.value();
		if (sourceRecordValue == null) {
			return;
		}

		try {
			if (sourceRecordValue.getString("ddl") != null) {
				if (!sinkDdl) {
					log.info("SKIP DDL: ======> " + sourceRecordValue.getString("ddl"));
					return;
				}
			}
		} catch (Exception e) {
		}

		try {
			log.info("struct ---->"+sourceRecord.value());
			
			Operation operation = super.getOperationType(sourceRecord);

			if (operation != null && operation != Operation.READ) {
				
				String source = getFormattedSource(sourceRecord);
				
				Map<String, Object> oldMessages = super.getOldRecord(sourceRecord);
				Map<String, Object> newMessages = super.getNewRecord(sourceRecord);
				
				log.info("Data Changed with Operation: {} -> old: {}, new: {}", operation.name(), oldMessages,
						newMessages);

				for (Map.Entry<String, SinkService> sinkServiceEntry : sinkServiceMap.entrySet()) {
					
					try {
						String sinkName = sinkServiceEntry.getKey();
						SinkService sinkService = sinkServiceEntry.getValue();
						
						MessageSinkModel messageSinkModel = MessageSinkModel.builder().sourceRecord(sourceRecord).source(source)
								.operation(operation).oldMessages(oldMessages).newMessages(newMessages)
								.debeziumPipeline(debeziumPipeline)
								.sinkName(sinkName)
								.build();

						sinkService.sink(messageSinkModel);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		} catch(org.apache.kafka.connect.errors.DataException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
