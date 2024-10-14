package co.kaustab.cdc.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.debezium.util.Strings;
import lombok.Data;

@Data
public class CustomDebeziumConfiguration implements io.debezium.config.Configuration {

	Map<String, Object> props = new HashMap<>();
	
	public CustomDebeziumConfiguration() {}
	
	public CustomDebeziumConfiguration(Map<String, ?> properties) {
		if (properties != null) {
            props.putAll(properties);
        }
	}
	
    @Override
    public Set<String> keys() {
        return props.keySet();
    }
    
    @Override
    public String getString(String key) {
		Object value = props.get(key);
		if (value == null) {
            return null;
        }
        if (value instanceof Collection<?>) {
            return Strings.join(",", (List<?>) value);
        }
        return value.toString();
    }

    @Override
    public String toString() {
        return withMaskedPasswords().asProperties().toString();
    }
}
