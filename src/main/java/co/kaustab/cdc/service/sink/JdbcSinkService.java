package co.kaustab.cdc.service.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import co.kaustab.cdc.config.JdbcSinkRegistry;
import co.kaustab.cdc.model.MessageSinkModel;
import co.kaustab.cdc.model.PipelineSinkConfig;
import co.kaustab.cdc.utils.Operation;
import co.kaustab.cdc.utils.RdbmsCliUtils;

@Service(value = JdbcSinkService.SERVICE_NAME)
public class JdbcSinkService implements SinkService {

	public static final String SERVICE_NAME = "jdbcSink";

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private RdbmsCliUtils rdbmsCliUtils;

	@Override
	public void sink(MessageSinkModel messageSinkModel) throws Exception {

		PipelineSinkConfig sinkConfiguration = SinkService.getPipelineSinkConfig(messageSinkModel.getDebeziumPipeline(),
				messageSinkModel.getSinkName());

		String tableName = messageSinkModel.getSource();
		String whiteListedTableNames = sinkConfiguration.getWhitelistedTables();
		if(!whiteListedTableNames.contains(tableName)) {
			return;
		}
		
		String db = sinkConfiguration.getJdbcDatabase();
		JdbcTemplate jdbcTemplate = (JdbcTemplate) applicationContext.getBean(JdbcSinkRegistry.getJdbcTemplateName(db));

		String mode = sinkConfiguration.getInsertMode();
		Operation operation = messageSinkModel.getOperation();
		if (operation == Operation.CREATE || mode.equalsIgnoreCase("insert")) {
			rdbmsCliUtils.insert(messageSinkModel, jdbcTemplate);
		} else if (operation == Operation.DELETE) {
			rdbmsCliUtils.delete(messageSinkModel, jdbcTemplate, sinkConfiguration);
		} else if (operation == Operation.UPDATE) {
			rdbmsCliUtils.update(messageSinkModel, jdbcTemplate, sinkConfiguration);
		}

	}
}
