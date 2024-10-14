package co.kaustab.cdc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineSource {

	@SerializedName("class")
	private String clazz;
	
	@SerializedName("source_config")
	private DebeziumSourceConnectorConfiguration debeziumSourceConnectorConfiguration;
	
}
