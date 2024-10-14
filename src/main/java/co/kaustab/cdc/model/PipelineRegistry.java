package co.kaustab.cdc.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class PipelineRegistry {

	@SerializedName("pipeline_metadata_files")
	private List<String> registryFiles;

}
