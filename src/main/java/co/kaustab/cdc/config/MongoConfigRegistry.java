package co.kaustab.cdc.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import co.kaustab.cdc.model.BmMongoDatabase;
import co.kaustab.cdc.model.PipelineSink;
import co.kaustab.cdc.utils.JasyptUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoConfigRegistry implements SinkConfigRegistry {

	private List<String> registeredMongoDatabases = new ArrayList<>(); // mongoclient per db

	/**
	 * The method creates exactly one mongo client per mogo database that can be
	 * used to perform crud operation
	 */
	@Override
	public void register(ConfigurableApplicationContext context, PipelineSink pipelineSink) throws Exception {

		JasyptUtils jasyptUtils = (JasyptUtils) context.getBean("jasyptUtils");

		String host = pipelineSink.getPipelineSinkConfig().getMongoDbHost();
		int port = pipelineSink.getPipelineSinkConfig().getMongoDbPort();
		String user = pipelineSink.getPipelineSinkConfig().getConnectionUserName();
		char[] password = jasyptUtils.decrypt(pipelineSink.getPipelineSinkConfig().getConnectionPassword())
				.toCharArray();
		String dbName = pipelineSink.getPipelineSinkConfig().getJdbcDatabase();

		// ensure exactly one mongo client created
		String mongoDbName = getMongoDbName(host, String.valueOf(port), dbName);
		if (registeredMongoDatabases.contains(mongoDbName)) {
			return;
		}

		MongoCredential credential = MongoCredential.createCredential(user, dbName, password);
		MongoClient mongoClient = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));

		GenericBeanDefinition mongoClientGenericBeanDefinition = new GenericBeanDefinition();
		mongoClientGenericBeanDefinition.setBeanClass(BmMongoDatabase.class);
		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addGenericArgumentValue(mongoClient);
		constructorArgumentValues.addGenericArgumentValue(dbName);
		mongoClientGenericBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);

		((GenericWebApplicationContext) context).registerBeanDefinition(mongoDbName, mongoClientGenericBeanDefinition);
		log.info("context registered bean - bmmongodb with name - " + mongoDbName + " ===> "
				+ context.getBean(mongoDbName));

		registeredMongoDatabases.add(mongoDbName);

	}

	public static String getMongoDbName(String host, String port, String db) {
		return "Mongo{" + host + ":" + port + "," + db + "}";
	}

}
