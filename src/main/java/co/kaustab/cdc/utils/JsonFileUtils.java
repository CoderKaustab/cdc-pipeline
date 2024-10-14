package co.kaustab.cdc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonFileUtils {

	public static Object readJsonFile(String jsonFilePath, Class clazz) throws Exception {
		String jsonData = readJsonFileAsString(jsonFilePath);
		Object object = convertJsonIntoClassListWithGson(jsonData, clazz);
		return object;
	}

	public static String readJsonFileAsString(String jsonFilePath) throws Exception {
		String jsonString = readFileAsString(jsonFilePath);
		return jsonString;
	}

	public static String readFileAsString(String file) throws Exception {
		StringBuffer jsonStringBuffer = new StringBuffer();
		String line = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream in = loader.getResourceAsStream(file);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		while ((line = bufferedReader.readLine()) != null) {
			jsonStringBuffer.append(line);
		}
		bufferedReader.close();
		return jsonStringBuffer.toString();
	}

	public static Object convertJsonIntoClassListWithGson(String jsonString, Class class_p) {
		return new Gson().fromJson(jsonString, class_p);
	}

	public static Object convertJsonIntoClassListWithJackson(String jsonString, Class class_p)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Object result = mapper.readValue(jsonString, class_p);
		return result;
	}
}
