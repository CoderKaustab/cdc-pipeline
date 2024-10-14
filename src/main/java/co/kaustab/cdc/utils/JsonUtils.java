package co.kaustab.cdc.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonUtils {

	public Map<String, Object> convertJsonIntoMap(String jsonString) {
		Map<String, Object> result = new HashMap<String, Object>();

		if (jsonString == null || jsonString.isEmpty()) {
			return result;
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object convertJsonIntoClass(String jsonString, Class class_p) {
		Object result = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(jsonString, class_p);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes" })
	public Object convertJsonIntoTypeReference(String jsonString, TypeReference typeReference) {
		Object result = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(jsonString, typeReference);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public Object convertJsonIntoClassList(String jsonString, Class class_p) {
		Object result = null;
		try {
			ObjectMapper mapper = new ObjectMapper();

			JsonNode jsonObject = mapper.readTree(jsonString);

			if (jsonObject.has("Errors")) {
				JsonNode errors = jsonObject.get("Errors");
				// LOG.info(errors.get("ErrorCode"));
				// LOG.info(errors.get("Message"));
				return result;
			}

			if (!jsonObject.has("Result") || jsonObject.get("Result") == null) {
				return result;
			}
			result = mapper.readValue(jsonObject.get("Result").toString(),
					mapper.getTypeFactory().constructCollectionType(List.class, class_p));

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public String convertToJSON(Object inputObj) {
		ObjectMapper objectMapper = new ObjectMapper();
		String orderJson = null;
		try {
			orderJson = objectMapper.writeValueAsString(inputObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return orderJson;
	}

	public HashMap<String, String> convertJsonIntoMapOfTypeString(String retVal)
			throws JsonProcessingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		HashMap<String, String> logTokens = new HashMap<String, String>();

		JsonNode node = objectMapper.readTree(retVal);

		Iterator<Entry<String, JsonNode>> itr = node.fields();

		while (itr.hasNext()) {
			Entry<String, JsonNode> entry = itr.next();

			String key = entry.getKey();

			JsonNode jsonNode = entry.getValue();

			if (jsonNode.isObject()) {
				traverseJsonObject(jsonNode, logTokens);
			} else {
				logTokens.put(key, jsonNode.asText());
			}
		}
		return logTokens;
	}

	private void traverseJsonObject(JsonNode jsonNode, HashMap<String, String> hashMap) {

		if (jsonNode.isObject()) {

			Iterator<Entry<String, JsonNode>> itr = jsonNode.fields();

			while (itr.hasNext()) {
				Entry<String, JsonNode> entry = itr.next();

				String key = entry.getKey();

				JsonNode jNode = entry.getValue();

				if (jNode.isObject()) {
					traverseJsonObject(jNode, hashMap);
				} else if (jNode.isTextual()) {
					hashMap.put(key, jNode.asText());
				}
			}
		}
	}

}
