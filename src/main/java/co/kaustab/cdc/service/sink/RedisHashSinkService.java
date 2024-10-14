package co.kaustab.cdc.service.sink;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import co.kaustab.cdc.config.RedisConfigRegistry;
import co.kaustab.cdc.model.DebeziumPipeline;
import co.kaustab.cdc.model.MessageSinkModel;
import co.kaustab.cdc.model.PipelineSinkConfig;
import co.kaustab.cdc.utils.Operation;
import co.kaustab.cdc.utils.RedisCliUtils;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
@Service(value = RedisHashSinkService.SERVICE_NAME)
public class RedisHashSinkService implements SinkService {

	public static final String SERVICE_NAME = "redisHashSink";

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private RedisCliUtils redisCliUtils;
	
	@Override
	public void sink(MessageSinkModel messageSinkModel) throws Exception {

		String source = messageSinkModel.getSource();
		Map<String, Object> newMessages = messageSinkModel.getNewMessages();
		Map<String, Object> oldMessages = messageSinkModel.getOldMessages();
		Operation operation = messageSinkModel.getOperation();
		DebeziumPipeline debeziumPipeline = messageSinkModel.getDebeziumPipeline();

		log.info("source = " + source + ", old data = " + oldMessages + ", new data = " + newMessages);

		PipelineSinkConfig pipelineSinkConfig = SinkService.getPipelineSinkConfig(debeziumPipeline, messageSinkModel.getSinkName());
		String redisClientName = RedisConfigRegistry.getRedisClientName(pipelineSinkConfig.getRedisServerHost(), pipelineSinkConfig.getRedisServerPort());
		Jedis jedis = (Jedis) applicationContext.getBean(redisClientName);

		if(!pipelineSinkConfig.getWhitelistedTables().contains(source)) {
			return;
		}
		
		String insertMode = pipelineSinkConfig.getInsertMode();
		String primaryField = pipelineSinkConfig.getPrimaryFields();

		String hashName = null;
		if (insertMode.equalsIgnoreCase("upsert")) {
			if (oldMessages != null && newMessages != null
					&& !newMessages.get(primaryField).equals(oldMessages.get(primaryField))) {
				hashName = getHashName(oldMessages, source, pipelineSinkConfig);
				redisCliUtils.hdelAll(jedis, hashName);
				hashName = getHashName(newMessages, source, pipelineSinkConfig);
				redisCliUtils.hsetAll(jedis, hashName, source, newMessages, pipelineSinkConfig);
				return;
			}
		}
		
		if(hashName == null || hashName.isEmpty()) {
			hashName = getHashName(newMessages, source, pipelineSinkConfig);
		}

		if (operation == Operation.CREATE || operation == Operation.UPDATE) {
			redisCliUtils.hsetAll(jedis, hashName, source, newMessages, pipelineSinkConfig);
		} else if (operation == Operation.DELETE) {
			redisCliUtils.hdelAll(jedis, hashName);
		}
	}

	protected String getHashName(Map<String, Object> cdcData, String source, PipelineSinkConfig pipelineSinkConfig) {
		String sourceAlias = (pipelineSinkConfig.getTableNameAliases() != null)
				? pipelineSinkConfig.getTableNameAliases().get(source)
				: null;
		String hashName = (sourceAlias != null && !sourceAlias.isEmpty() ? sourceAlias : source) + "."
				+ cdcData.get("id");
		return hashName;
	}

}
