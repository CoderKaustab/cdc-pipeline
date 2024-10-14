package co.kaustab.cdc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
		MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
public class CDCApplication {

	public static void main(String[] args) throws Exception {

		SpringApplication.run(CDCApplication.class, args);

	}
}
