package co.kaustab.cdc.service.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoDatabase;

import co.kaustab.cdc.config.MongoConfigRegistry;
import co.kaustab.cdc.model.BmMongoDatabase;
import co.kaustab.cdc.model.MessageSinkModel;
import co.kaustab.cdc.model.PipelineSinkConfig;
import co.kaustab.cdc.utils.MongoCliUtils;
import co.kaustab.cdc.utils.Operation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = MongoSinkService.SERVICE_NAME)
@SuppressWarnings("unchecked")
public class MongoSinkService implements SinkService {

	public static final String SERVICE_NAME = "mongoSink";
	
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private MongoCliUtils mongoCliUtils;
	
	@Override
	public void sink(MessageSinkModel messageSinkModel) throws Exception {
		
		PipelineSinkConfig sinkConfiguration = SinkService.getPipelineSinkConfig(messageSinkModel.getDebeziumPipeline(),
				messageSinkModel.getSinkName());
		
		String host = sinkConfiguration.getMongoDbHost();
		String port = String.valueOf(sinkConfiguration.getMongoDbPort());
		String db = sinkConfiguration.getJdbcDatabase();
		
		String mongoDbName = MongoConfigRegistry.getMongoDbName(host, port, db);
		BmMongoDatabase bmMongoDatabase = (BmMongoDatabase) applicationContext.getBean(mongoDbName);
		
		MongoDatabase mongoDatabase = bmMongoDatabase.getMongoDatabase();
	
		String mode = sinkConfiguration.getInsertMode();
		Operation operation = messageSinkModel.getOperation();

		if (operation == Operation.CREATE) {
			mongoCliUtils.insert(mongoDatabase, messageSinkModel);
		} else if (operation == Operation.DELETE) {
			mongoCliUtils.delete(mongoDatabase, messageSinkModel, sinkConfiguration);
		} else if (operation == Operation.UPDATE) {
			mongoCliUtils.update(mongoDatabase, messageSinkModel, sinkConfiguration);
		}
	}

}
