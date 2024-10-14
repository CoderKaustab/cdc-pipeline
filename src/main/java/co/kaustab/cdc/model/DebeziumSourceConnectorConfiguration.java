package co.kaustab.cdc.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebeziumSourceConnectorConfiguration {

	@SerializedName("name")
	private String connectorName;
	
	@SerializedName("config")
	private Map<String, String> connectorConfigs;
	
}
