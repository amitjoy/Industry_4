package de.tum.in.mongodb;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongoDBServiceImpl implements MongoDBService {
	private final Mongo m_mongo;
	private final DB m_db;

	public MongoDBServiceImpl(Mongo mongo, DB db) {
		m_mongo = mongo;
		m_db = db;
	}

	@Override
	public DB getDB() {
		return m_db;
	}

	public void close() {
		m_mongo.close();
	}

	public void stop() {
		close();
	}
}
