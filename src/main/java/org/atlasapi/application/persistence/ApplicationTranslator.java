package org.atlasapi.application.persistence;

import org.atlasapi.application.Application;

import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ApplicationTranslator {
	
	public static final String APPLICATION_SLUG_KEY = MongoConstants.ID;
	public static final String APPLICATION_TITLE_KEY= "title";
	public static final String APPLICATION_CONFIG_KEY="configuration";
	public static final String APPLICATION_CREDENTIALS_KEY="credentials";
	
	private final ApplicationConfigurationTranslator configurationTranslator = new ApplicationConfigurationTranslator();
	private final ApplicationCredentialsTranslator credentialsTranslator = new ApplicationCredentialsTranslator();
	
	public DBObject toDBObject(Application application) {
		DBObject dbo = new BasicDBObject();
		
		dbo.put(APPLICATION_SLUG_KEY, application.getSlug());
		dbo.put(APPLICATION_TITLE_KEY, application.getTitle());
		dbo.put(APPLICATION_CONFIG_KEY, configurationTranslator.toDBObject(application.getConfiguration()));
		dbo.put(APPLICATION_CREDENTIALS_KEY, credentialsTranslator.toDBObject(application.getCredentials()));
		
		return dbo;
	}
	
	public Application fromDBObject(DBObject dbo) {
		String applicationSlug = (String) dbo.get(APPLICATION_SLUG_KEY);
		
		if(applicationSlug == null){
			return null;
		}
		
		Application application = new Application(applicationSlug);
		
		application.setTitle((String) dbo.get(APPLICATION_TITLE_KEY));
		application.setConfiguration(configurationTranslator.fromDBObject((DBObject)dbo.get(APPLICATION_CONFIG_KEY)));
		application.setCredentials(credentialsTranslator.fromDBObject((DBObject)dbo.get(APPLICATION_CREDENTIALS_KEY)));
		
		return application;
	}

}
