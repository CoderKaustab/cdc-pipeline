package co.kaustab.cdc.config;

import org.springframework.context.ConfigurableApplicationContext;

import co.kaustab.cdc.model.PipelineSink;

public interface SinkConfigRegistry {

	void register(ConfigurableApplicationContext context, PipelineSink pipelineSink) throws Exception;

}
