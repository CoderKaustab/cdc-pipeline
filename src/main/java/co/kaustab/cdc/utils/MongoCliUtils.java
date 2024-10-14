package co.kaustab.cdc.utils;

import static com.mongodb.client.model.Filters.eq;

import java.util.Map;

import org.bson.BsonValue;
import org.bson.Document;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import co.kaustab.cdc.model.MessageSinkModel;
import co.kaustab.cdc.model.PipelineSinkConfig;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class MongoCliUtils {

	public void insert(MongoDatabase mongoDatabase, MessageSinkModel messageSinkModel) {

		// same as source table/collection name
		String sinkCollectionName = messageSinkModel.getSource().split("\\.")[1];

		MongoCollection<Document> collection = mongoDatabase.getCollection(sinkCollectionName);
		Document newDocument = new Document(messageSinkModel.getNewMessages());
		collection.insertOne(newDocument);
	}

	public void update(MongoDatabase mongoDatabase, MessageSinkModel messageSinkModel,
			PipelineSinkConfig sinkConfiguration) {

		String sinkCollectionName = messageSinkModel.getSource().split("\\.")[1];
		Map<String, Object> newData = messageSinkModel.getNewMessages();
		Map<String, Object> oldData = messageSinkModel.getOldMessages();
		String pkId = sinkConfiguration.getPrimaryFields();
		Object pkVal = oldData.get(pkId);
		
		MongoCollection<Document> collection = mongoDatabase.getCollection(sinkCollectionName);

		Document existingDocument = getDocument(mongoDatabase, sinkCollectionName, pkId, oldData.get(pkId));
		if (existingDocument == null || sinkConfiguration.getInsertMode().equalsIgnoreCase("insert")) {
			insert(mongoDatabase, messageSinkModel);
			return;
		}

		existingDocument.clear();
		existingDocument.putAll(newData);
		
		UpdateResult resp = collection.updateOne(eq(pkId, oldData.get(pkId)), existingDocument);
		BsonValue upsertId = resp.getUpsertedId();
		log.info("Mongodb update completed = "+upsertId.asString());
	}

	private Document getDocument(MongoDatabase mongoDatabase, String sinkCollectionName, String pkId, Object pkVal) {
		MongoCollection<Document> collection = mongoDatabase.getCollection(sinkCollectionName);
		Document document = collection.find(eq(pkId, pkVal)).first();
		return document;
	}

	public void delete(MongoDatabase mongoDatabase, MessageSinkModel messageSinkModel,
			PipelineSinkConfig sinkConfiguration) {
		
		String pkId = sinkConfiguration.getPrimaryFields();
		Map<String, Object> oldData = messageSinkModel.getOldMessages();
		Object pkVal = oldData.get(pkId);
		
		String sinkCollectionName = messageSinkModel.getSource().split("\\.")[1];
		MongoCollection<Document> collection = mongoDatabase.getCollection(sinkCollectionName);
		DeleteResult resp = collection.deleteOne(eq(pkId, pkVal));
		long count = resp.getDeletedCount();
		log.info("Mongodb delete count = "+count);
	}

}