package co.kaustab.cdc.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.kaustab.cdc.model.DebeziumPipeline;
import co.kaustab.cdc.model.PipelineRegistry;
import co.kaustab.cdc.model.PipelineSink;
import co.kaustab.cdc.service.sink.JdbcSinkService;
import co.kaustab.cdc.service.sink.KafkaMessageProducer;
import co.kaustab.cdc.service.sink.MongoSinkService;
import co.kaustab.cdc.service.sink.RedisHashSinkService;
import co.kaustab.cdc.utils.JsonFileUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * This class bootstraps the CDC pipeline configuration defined in the pipeline registry file
 */
@Slf4j
@Configuration
public class DynamicPipelineRegistrationConfiguration {

	@Value("${pipeline.registry.file}")
	private String PIPELINE_REGISTRY_FILE;

	@Bean(name = "pipeLineConfigs")
	public Map<String, DebeziumPipeline> cdcConfigurations(ConfigurableApplicationContext context) throws Exception {

		Map<String, DebeziumPipeline> deployedPipelineMap = new HashMap<>();

		// load the Main registry file containing all the pipeline registries
		PipelineRegistry pipelineRegistry = (PipelineRegistry) JsonFileUtils.readJsonFile(PIPELINE_REGISTRY_FILE,
				PipelineRegistry.class);
		List<String> pipelineRegistryFiles = pipelineRegistry.getRegistryFiles();
		if (pipelineRegistryFiles == null || pipelineRegistryFiles.isEmpty()) {
			return null;
		}

		for (String pipelineRegistryFile : pipelineRegistryFiles) {
			
			log.info("pipelineRegistryFile -----> " + pipelineRegistryFile);

			DebeziumPipeline debeziumPipeline = (DebeziumPipeline) JsonFileUtils.readJsonFile(pipelineRegistryFile,
					DebeziumPipeline.class);
			
			// validate sinks
			List<PipelineSink> pipelineSinks = debeziumPipeline.getPipelineSinks();
			if (pipelineSinks == null || pipelineSinks.isEmpty()) {
				throw new Exception("Pipeline should have atleast one sink");
			}

			// initialize the pipeline sinks
			for (PipelineSink pipelineSink : pipelineSinks) {
				initSink(context, pipelineSink);
			}

			// initialize the pipeline source (Debezimum connector)
			CdcListenerRegistrationConfig.initCdcListener(context, debeziumPipeline);
		}

		log.info("deployedPipelineMap ----> " + deployedPipelineMap);
		return deployedPipelineMap;
	}

	private void initSink(ConfigurableApplicationContext context, PipelineSink pipelineSink)
			throws Exception {

		String sinkServiceBeanName = pipelineSink.getServiceBeanName();
		
		if(sinkServiceBeanName.equals(JdbcSinkService.SERVICE_NAME)) {
			new JdbcSinkRegistry().register(context, pipelineSink);
		}else if (sinkServiceBeanName.equals(KafkaMessageProducer.SERVICE_NAME)) {
			new KafkaProducerRegistry().register(context, pipelineSink);
		}else if (sinkServiceBeanName.equals(RedisHashSinkService.SERVICE_NAME)) {
			new RedisConfigRegistry().register(context, pipelineSink);
		}else if (sinkServiceBeanName.equals(MongoSinkService.SERVICE_NAME)) {
			new MongoConfigRegistry().register(context, pipelineSink);
		}else {
			throw new RuntimeException("No sink config registry found...");
		}
	}
}
