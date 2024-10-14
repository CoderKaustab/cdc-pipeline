package co.kaustab.cdc.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebeziumPipeline {

	@SerializedName("pipeline_name")
	private String pipelineName;

	@SerializedName("source")
	private PipelineSource pipelineSource;

	@SerializedName("sinks")
	private List<PipelineSink> pipelineSinks;

}
