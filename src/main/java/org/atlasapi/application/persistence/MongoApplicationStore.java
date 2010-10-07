package org.atlasapi.application.persistence;

import org.atlasapi.application.model.Application;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class MongoApplicationStore implements ApplicationPersistor, ApplicationReader {
	
	public static final String APPLICATION_COLLECTION = "applications";
	
	private final ApplicationTranslator translator = new ApplicationTranslator();
	
	private final DBCollection applications;
	
	public MongoApplicationStore(DatabasedMongo mongo) {
		this.applications = mongo.collection(APPLICATION_COLLECTION);
	}

	@Override
	public Application applicationFor(String slug) {
		return translator.fromDBObject(applications.findOne(new BasicDBObject(ApplicationTranslator.APPLICATION_SLUG_KEY, slug)));
	}

	@Override
	public void persist(Application application) {
		applications.insert(translator.toDBObject(application));
	}

}
