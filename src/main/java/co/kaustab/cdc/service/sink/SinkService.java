package co.kaustab.cdc.service.sink;

import co.kaustab.cdc.model.DebeziumPipeline;
import co.kaustab.cdc.model.MessageSinkModel;
import co.kaustab.cdc.model.PipelineSinkConfig;

public interface SinkService {

	public static PipelineSinkConfig getPipelineSinkConfig(DebeziumPipeline debeziumPipeline, String sinkName) {
		return debeziumPipeline.getPipelineSinks().stream().filter(sink -> sink.getName().equals(sinkName)).findFirst()
				.get().getPipelineSinkConfig();

	}

	public void sink(MessageSinkModel messageSinkModel) throws Exception;

}
