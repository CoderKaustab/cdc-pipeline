package co.kaustab.cdc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;
import com.google.gson.annotations.Expose;

@Getter
@Setter
public class PipelineSink {

	@SerializedName("name")
	private String name;
	
	@SerializedName("bean_service")
	private String serviceBeanName;
	
	@SerializedName("sink_config")
	private PipelineSinkConfig pipelineSinkConfig;
	
}
