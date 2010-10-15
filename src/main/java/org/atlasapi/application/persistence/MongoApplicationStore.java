package org.atlasapi.application.persistence;

import java.net.InetAddress;
import java.util.Set;

import org.atlasapi.application.Application;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoApplicationStore implements ApplicationStore {
	
	public static final String APPLICATION_COLLECTION = "applications";
	
	private final ApplicationTranslator translator = new ApplicationTranslator();
	
	private final DBCollection applications;

	private DatabasedMongo mongo;
	
	public MongoApplicationStore(DatabasedMongo mongo) {
		this.applications = mongo.collection(APPLICATION_COLLECTION);
		this.mongo = mongo;
	}
	
	@Override
	public Application applicationForKey(String key) {
		String credentialsKey = ApplicationTranslator.APPLICATION_CREDENTIALS_KEY;
		String apiKeyKey = ApplicationCredentialsTranslator.API_KEY_KEY;
		
		return translator.fromDBObject(applications.findOne(new BasicDBObject(credentialsKey+"."+apiKeyKey, key)));
	}

	@Override
	public Application applicationFor(String slug) {
		return translator.fromDBObject(applications.findOne(new BasicDBObject(ApplicationTranslator.APPLICATION_SLUG_KEY, slug)));
	}
	

	@Override
	public Application applicationForAddress(InetAddress address) {
		String credentialsKey = ApplicationTranslator.APPLICATION_CREDENTIALS_KEY;
		String ipKey = ApplicationCredentialsTranslator.IP_ADDRESS_KEY;
		return translator.fromDBObject(applications.findOne(new BasicDBObject(credentialsKey+"."+ipKey, address.getHostAddress())));
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
		CommandResult result = mongo.database().getLastError();
		if (result.get("err") != null && result.getInt("code") == 11000) {
			throw new IllegalArgumentException("Duplicate application slug");
		}
	}
	
	@Override
	public void update(Application application) {
		applications.update(new BasicDBObject(MongoConstants.ID, application.getSlug()), translator.toDBObject(application));
	}
}
