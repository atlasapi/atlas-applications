package org.atlasapi.application.persistence;

import java.util.Set;

import org.atlasapi.application.model.Application;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

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
	public Set<Application> applications() {
		return ImmutableSet.copyOf(Iterables.transform(applications.find(), new Function<DBObject, Application>(){
			@Override
			public Application apply(DBObject dbo) {
				return translator.fromDBObject(dbo);
			}
		}));
	}

	@Override
	public void persist(Application application) {
		applications.insert(translator.toDBObject(application));
	}
	
}
