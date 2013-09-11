package org.atlasapi.application;

import org.atlasapi.application.OldApplication;

import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ApplicationTranslator {
	
	public static final String APPLICATION_SLUG_KEY = MongoConstants.ID;
	public static final String APPLICATION_TITLE_KEY = "title";
	public static final String APPLICATION_DESCRIPTION_KEY = "desc";
	public static final String APPLICATION_CREATED_KEY = "created";
	public static final String APPLICATION_CONFIG_KEY = "configuration";
	public static final String APPLICATION_CREDENTIALS_KEY = "credentials";
	
	private final ApplicationConfigurationTranslator configurationTranslator = new ApplicationConfigurationTranslator();
	private final ApplicationCredentialsTranslator credentialsTranslator = new ApplicationCredentialsTranslator();
	
	public DBObject toDBObject(OldApplication application) {
		DBObject dbo = new BasicDBObject();
		
		if (application != null) {
		    TranslatorUtils.from(dbo, APPLICATION_SLUG_KEY, application.getSlug());
		    TranslatorUtils.from(dbo, APPLICATION_TITLE_KEY, application.getTitle());
		    TranslatorUtils.from(dbo, APPLICATION_DESCRIPTION_KEY, application.getDescription());
		    TranslatorUtils.fromDateTime(dbo, APPLICATION_CREATED_KEY, application.getCreated());
		    TranslatorUtils.from(dbo, APPLICATION_CONFIG_KEY, configurationTranslator.toDBObject(application.getConfiguration()));
		    TranslatorUtils.from(dbo, APPLICATION_CREDENTIALS_KEY, credentialsTranslator.toDBObject(application.getCredentials()));
		}
		
		return dbo;
	}
	
	public OldApplication fromDBObject(DBObject dbo) {
	    if (dbo == null) {
	        return null;
	    }
	    
		String applicationSlug = TranslatorUtils.toString(dbo, APPLICATION_SLUG_KEY);
		if(applicationSlug == null){
			return null;
		}
		
		return OldApplication.application(applicationSlug)
		        .withTitle(TranslatorUtils.toString(dbo, APPLICATION_TITLE_KEY))
		        .withDescription(TranslatorUtils.toString(dbo, APPLICATION_DESCRIPTION_KEY))
		        .createdAt(TranslatorUtils.toDateTime(dbo, APPLICATION_CREATED_KEY))
		        .withConfiguration(configurationTranslator.fromDBObject(TranslatorUtils.toDBObject(dbo, APPLICATION_CONFIG_KEY)))
		        .withCredentials(credentialsTranslator.fromDBObject(TranslatorUtils.toDBObject(dbo, APPLICATION_CREDENTIALS_KEY))).build();
	}

}
