package co.kaustab.cdc.model;

import com.mongodb.MongoClient;

import lombok.Getter;

@Getter
public class BmMongoDatabase {

	private com.mongodb.client.MongoDatabase mongoDatabase;
	
	public BmMongoDatabase(MongoClient mongoClient, String dbName) {
		this.mongoDatabase = mongoClient.getDatabase(dbName);
	}
	
}
