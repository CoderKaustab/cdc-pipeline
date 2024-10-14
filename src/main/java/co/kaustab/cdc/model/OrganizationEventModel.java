package co.kaustab.cdc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationEventModel {
	
	@JsonProperty("organization_uuid")
	private String organizationUuid;
	
	@JsonProperty("org_slug")
	private String orgSlug;
	
	@JsonProperty("org_name")
	private String orgName;
	
	@JsonProperty("short_name")
	private String shortName;
	
	@JsonProperty("legacy_uuid")
	private String legacyUuid;
	
}