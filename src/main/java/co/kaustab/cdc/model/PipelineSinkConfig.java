package co.kaustab.cdc.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineSinkConfig {

	// redis sink fields
	@SerializedName("insert.mode")
	private String insertMode;

	@SerializedName("pk.fields")
	private String primaryFields;

	@SerializedName("whitelisted_columns")
	private Map<String, String> whitelistedColumns;

	@SerializedName("whitelisted_tables")
	private String whitelistedTables;

	@SerializedName("table_name_aliases")
	private Map<String, String> tableNameAliases;

	@SerializedName("column_name_aliases")
	private Map<String, String> columnNameAliases;


	// additional kafka sink fields
	@SerializedName("producer_config")
	private Map<String, String> producerConfig;
	
	@SerializedName("messages")
	private List<KafkaMessageSettings> messageSettings;

	@Data
	public static class KafkaMessageSettings {

		@SerializedName("message_sources")
		private String messageSources;

		@SerializedName("message_sinks")
		private List<KafkaTopicMessageSettings> kafkaTopicMessageSettings;

	}

	@Data
	public static class KafkaTopicMessageSettings {

		@SerializedName("topics")
		private String topics;

		@SerializedName("message_builder_component")
		private String messageBuilderComponent;
	}
	
	// additional jdbc sink configs
	@SerializedName("database.whitelist")
	private String jdbcDatabase;

	@SerializedName("connection.url")
	private String connectionUrl;
	
	@SerializedName("connection.username")
	private String connectionUserName;
	
	@SerializedName("connection.password")
	private String connectionPassword;
	
	// redis config
	@SerializedName("redis.server.host")
	private String redisServerHost;
	
	@SerializedName("redis.server.port")
	private String redisServerPort;

	// mongo
	@SerializedName("mongo.db.host")
	private String mongoDbHost;
	
	@SerializedName("mongo.db.port")
	private int mongoDbPort;

}
