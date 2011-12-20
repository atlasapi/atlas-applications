package org.atlasapi.application.persistence;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.NO_UPSERT;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.SINGLE;

import java.util.Set;

import org.atlasapi.application.Application;
import org.atlasapi.application.users.User;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoApplicationStore implements ApplicationStore {
	
	public static final String APPLICATION_COLLECTION = "applications";
	
	private final ApplicationTranslator translator = new ApplicationTranslator();
	
	private final DBCollection applications;

	private DatabasedMongo mongo;

    private final Function<DBObject, Application> translatorFunction = new Function<DBObject, Application>(){
        @Override
        public Application apply(DBObject dbo) {
            return translator.fromDBObject(dbo);
        }
    };
	
	public MongoApplicationStore(DatabasedMongo mongo) {
		this.applications = mongo.collection(APPLICATION_COLLECTION);
		this.mongo = mongo;
	}
	
	@Override
	public Optional<Application> applicationForKey(String key) {
		String apiKeyField = String.format("%s.%s", ApplicationTranslator.APPLICATION_CREDENTIALS_KEY, ApplicationCredentialsTranslator.API_KEY_KEY);
		return Optional.fromNullable(translator.fromDBObject(applications.findOne(where().fieldEquals(apiKeyField, key).build())));
	}

	@Override
	public Optional<Application> applicationFor(String slug) {
		return Optional.fromNullable(translator.fromDBObject(applications.findOne(where().idEquals(slug).build())));
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
		applications.update(where().idEquals(application.getSlug()).build(), translator.toDBObject(application), NO_UPSERT, SINGLE);
	}

    @Override
    public Set<Application> applicationsFor(Optional<User> user) {
        if (!user.isPresent()) {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(Iterables.transform(applications.find(where().idIn(user.get().getApplications()).build()), translatorFunction));
    }
	
}
