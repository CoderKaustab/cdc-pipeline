package co.kaustab.cdc.config;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.support.GenericWebApplicationContext;

import co.kaustab.cdc.model.PipelineSink;
import co.kaustab.cdc.model.PipelineSinkConfig;
import co.kaustab.cdc.utils.JasyptUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcSinkRegistry implements SinkConfigRegistry {

	private static List<String> registeredJdbcTemplates = new ArrayList<>(); 	// static memory of the databases for which
																				// jdbc template is registered in the
																				// spring context

	/**
	 * The method registers a jdbc template in the spring context for each database
	 * that can be used in the application for database crud operations
	 */
	@Override
	public void register(ConfigurableApplicationContext context, PipelineSink pipelineSink) {

		// get the sink configuration (in this case sink is a database)
		PipelineSinkConfig sinkConfig = pipelineSink.getPipelineSinkConfig();

		// get the database name
		String databaseName = sinkConfig.getJdbcDatabase();

		// check if JdbcTemplate is already registered in Spring context for the current
		// database
		if (JdbcSinkRegistry.registeredJdbcTemplates.contains(databaseName)) {
			return;
		}

		// decrypt the database password
		JasyptUtils jasyptUtils = (JasyptUtils) context.getBean("jasyptUtils");
		String password = jasyptUtils.decrypt(sinkConfig.getConnectionPassword());

		// create data source object
		DataSource dataSource = DataSourceBuilder.create().url(sinkConfig.getConnectionUrl())
				.username(sinkConfig.getConnectionUserName()).password(password).build();

		// create jdbc template bean definition with the create data source
		GenericBeanDefinition jdbcGenericBeanDefinition = new GenericBeanDefinition();
		jdbcGenericBeanDefinition.setBeanClass(JdbcTemplate.class);
		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addGenericArgumentValue(dataSource);
		jdbcGenericBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);

		// get jdbc template bean name
		String jdbcTemplateName = getJdbcTemplateName(databaseName);

		// register the jdbc template bean in the spring context
		((GenericWebApplicationContext) context).registerBeanDefinition(jdbcTemplateName, jdbcGenericBeanDefinition);
		log.info("context registered bean - jdbcTemplate with name - " + jdbcTemplateName + " ===> "
				+ context.getBean(jdbcTemplateName));

		// add the database name the static memory so that we can avoid creating
		// duplicate jdbc template for the same database next time
		registeredJdbcTemplates.add(databaseName);

	}

	/**
	 * @return the jdbc template bean name for a specific database
	 */
	public static String getJdbcTemplateName(String databaseName) {
		return "JdbcTemplate{" + databaseName + "}";
	}

}
