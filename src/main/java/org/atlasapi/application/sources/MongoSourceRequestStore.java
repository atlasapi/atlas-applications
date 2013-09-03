package org.atlasapi.application.sources;

import org.atlasapi.application.Application;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;

public class MongoSourceRequestStore implements SourceRequestStore {

public static final String SOURCE_REQUESTS_COLLECTION = "sourceRequests";
    
    private final SourceRequestTranslator translator = new SourceRequestTranslator();
    
    private final DBCollection sourceRequests;
    
    public MongoSourceRequestStore(DatabasedMongo mongo) {
        this.sourceRequests = mongo.collection(SOURCE_REQUESTS_COLLECTION);
    }
    @Override
    public void store(SourceRequest sourceRequest) {
        this.sourceRequests.save(translator.toDBObject(sourceRequest));
    }
    @Override
    public Optional<SourceRequest> getBy(Application application, Publisher publisher) {
        return Optional.fromNullable(translator.fromDBObject(this.sourceRequests.findOne(where().idEquals(translator.createKey(application, publisher)).build())));
    }
}
