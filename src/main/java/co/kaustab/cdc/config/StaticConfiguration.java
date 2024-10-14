package co.kaustab.cdc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.kaustab.cdc.utils.JsonUtils;

@Configuration
public class StaticConfiguration {

	@Bean
	public JsonUtils jsonUtils() {
		return new JsonUtils();
	}
}
