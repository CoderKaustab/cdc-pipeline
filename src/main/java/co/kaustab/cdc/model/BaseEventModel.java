package co.kaustab.cdc.model;

import java.util.Calendar;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEventModel {

	@JsonProperty("event_uuid")
	private String eventUuid;

	@JsonProperty("event_name")
	private String eventName;

	@JsonProperty("event_type")
	private String eventType;

	@JsonProperty("created_time")
	private Calendar createdTime;

	@JsonProperty("organization")
	private OrganizationEventModel organizationEventModel;

	public BaseEventModel() {
		this.eventUuid = UUID.randomUUID().toString();
	}

}