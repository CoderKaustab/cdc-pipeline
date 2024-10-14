package co.kaustab.cdc.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.kaustab.cdc.model.PipelineSinkConfig;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
@Component
public class RedisCliUtils {

	public void hdelAll(final Jedis jedis, final String hashName) throws Exception {
		Map<String, String> hashFields = jedis.hgetAll(hashName);
		if (hashFields != null && !hashFields.isEmpty()) {
			hashFields.keySet().forEach(field -> {
				jedis.hdel(hashName, field);
			});
		}
	}

	public void hsetAll(final Jedis jedis, final String hashName, final String sourceTable,
			final Map<String, Object> newData, PipelineSinkConfig pipelineSinkConfig) throws Exception {

		if (hashName == null || sourceTable == null) {
			return;
		}

		// filter white listed hash fields
		List<String> requiredRedisHashFields = (newData != null) ? new ArrayList<>(newData.keySet()) : Arrays.asList();
		if (pipelineSinkConfig.getWhitelistedColumns() != null
				&& pipelineSinkConfig.getWhitelistedColumns().containsKey(sourceTable)) {
			String whiteListedColumns = pipelineSinkConfig.getWhitelistedColumns().get(sourceTable);
			if (whiteListedColumns != null) {
				List<String> whitelistedHashFields = (whiteListedColumns.contains(","))
						? Arrays.asList(whiteListedColumns.split(","))
						: Arrays.asList(whiteListedColumns);
				if (whitelistedHashFields.isEmpty()) {
					return;
				} else {
					requiredRedisHashFields.retainAll(whitelistedHashFields);
				}
			}
		}

		// form hash data map
		Map<String, String> hashData = new HashMap<>();
		if (newData != null) {
			requiredRedisHashFields.forEach(hashField -> {
				String hashFieldAlias = hashField;
				Map<String, String> columnNameAliases = pipelineSinkConfig.getColumnNameAliases();
				if (columnNameAliases != null && !columnNameAliases.isEmpty()
						&& columnNameAliases.get(sourceTable + "." + hashField) != null
						&& !columnNameAliases.get(sourceTable + "." + hashField).isEmpty()) {
					hashFieldAlias = columnNameAliases.get(sourceTable + "." + hashField);
				}
				hashData.put(hashFieldAlias, newData.get(hashField).toString());
			});
		}

		// clean existing hash
		hdelAll(jedis, hashName);
		if (newData == null || hashData == null || hashData.isEmpty()) {
			return;
		}

		// set all hash data
		long resp = jedis.hset(hashName, hashData);
		log.info("Redis sink executed for hash: " + hashName + " with resp code: " + resp);
	}

}
