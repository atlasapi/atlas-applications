package org.atlasapi.application.v3;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.NO_UPSERT;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.SINGLE;
import static org.atlasapi.application.v3.ApplicationConfigurationTranslator.PUBLISHER_KEY;
import static org.atlasapi.application.v3.ApplicationConfigurationTranslator.SOURCES_KEY;
import static org.atlasapi.application.v3.ApplicationConfigurationTranslator.STATE_KEY;
import static org.atlasapi.application.v3.ApplicationTranslator.APPLICATION_CONFIG_KEY;

import java.util.Set;

import org.atlasapi.application.users.v3.User;
import org.atlasapi.application.v3.Application;
import org.atlasapi.application.v3.SourceStatus.SourceState;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.IdGenerator;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.text.MoreStrings;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;

public class MongoApplicationStore implements ApplicationStore {
	
	public static final String APPLICATION_COLLECTION = "applications";
	
	private final ApplicationTranslator translator = new ApplicationTranslator();
	
	private final DBCollection applications;

	private DatabasedMongo mongo;
	
	private final IdGenerator idGenerator;

    private final Function<DBObject, Application> translatorFunction = new Function<DBObject, Application>(){
        @Override
        public Application apply(DBObject dbo) {
            return translator.fromDBObject(dbo);
        }
    };
	
	public MongoApplicationStore(DatabasedMongo mongo, IdGenerator idGenerator) {
		this.applications = mongo.collection(APPLICATION_COLLECTION);
		this.applications.setReadPreference(ReadPreference.primary());
		this.mongo = mongo;
		this.idGenerator = idGenerator;
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
	public Application persist(Application application) {
	    // Check that an id is being generated for compatibility with 4.0
        if (application.getDeerId() == null) {
            application = application.copy().withDeerId(idGenerator.generateRaw()).build();
        }
		applications.insert(translator.toDBObject(application));
		CommandResult result = mongo.database().getLastError();
		if (result.get("err") != null && result.getInt("code") == 11000) {
			throw new IllegalArgumentException("Duplicate application slug");
		}
		return application;
	}
	
	@Override
	public Application update(Application application) {
	    // Check that an id is being generated for compatibility with 4.0
	    if (application.getDeerId() == null) {
	        application = application.copy().withDeerId(idGenerator.generateRaw()).build();
	    }
		applications.update(where().idEquals(application.getSlug()).build(), translator.toDBObject(application), NO_UPSERT, SINGLE);
		return application;
	}

    @Override
    public Set<Application> applicationsFor(Optional<User> user) {
        if (!user.isPresent()) {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(Iterables.transform(applications.find(where().idIn(user.get().getApplicationSlugs()).build()), translatorFunction));
    }

    @Override
    public Set<Application> applicationsFor(Publisher source) {
        String sourceField = String.format("%s.%s.%s", APPLICATION_CONFIG_KEY, SOURCES_KEY, PUBLISHER_KEY);
        String stateField =  String.format("%s.%s.%s", APPLICATION_CONFIG_KEY, SOURCES_KEY, STATE_KEY);
        return ImmutableSet.copyOf(Iterables.transform(applications.find(where().fieldEquals(sourceField, source.key()).fieldIn(stateField, states()).build()), translatorFunction));
    }

    private Iterable<String> states() {
        return Iterables.transform(ImmutableSet.of(SourceState.AVAILABLE, SourceState.REQUESTED), Functions.compose(MoreStrings.TO_LOWER, Functions.toStringFunction()));
    }

    @Override
    public Iterable<Application> allApplications() {
        return Iterables.transform(applications.find(where().build()), translatorFunction);
    }
	
}