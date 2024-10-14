package co.kaustab.cdc.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import co.kaustab.cdc.model.PipelineSink;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
public class RedisConfigRegistry implements SinkConfigRegistry {

	private static List<String> registeredRedisClients = new ArrayList<>();

	@Override
	public void register(ConfigurableApplicationContext context, PipelineSink pipelineSink) throws Exception {

		String host = pipelineSink.getPipelineSinkConfig().getRedisServerHost();
		String port = pipelineSink.getPipelineSinkConfig().getRedisServerPort();

		String redisClientName = getRedisClientName(host, port);
		if (registeredRedisClients.contains(redisClientName)) {
			return;
		}

		GenericBeanDefinition redisClientGenericBeanDefinition = new GenericBeanDefinition();
		redisClientGenericBeanDefinition.setBeanClass(Jedis.class);
		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addGenericArgumentValue(host);
		constructorArgumentValues.addGenericArgumentValue(port);
		redisClientGenericBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);

		((GenericWebApplicationContext) context).registerBeanDefinition(redisClientName,
				redisClientGenericBeanDefinition);
		log.info("context registered bean - redis client with name - " + redisClientName + " ===> "
				+ context.getBean(redisClientName));

		registeredRedisClients.add(redisClientName);
	}

	public static String getRedisClientName(String host, String port) {
		return "Redis{" + host + ":" + port + "}";
	}

}
