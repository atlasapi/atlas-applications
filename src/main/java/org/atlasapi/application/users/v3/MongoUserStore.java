package org.atlasapi.application.users.v3;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.SINGLE;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.UPSERT;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.translator.UserRefTranslator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoUserStore implements UserStore {

    private DBCollection users;
    private UserTranslator translator;
    private UserRefTranslator userRefTranslator;

    public MongoUserStore(DatabasedMongo mongo) {
        this.users = mongo.collection("users");
        this.userRefTranslator = new UserRefTranslator();
        this.translator = new UserTranslator(userRefTranslator);
    }
    
    private final Function<DBObject, User> translatorFunction = new Function<DBObject, User>(){
        @Override
        public User apply(DBObject dbo) {
            return translator.fromDBObject(dbo);
        }
    };
    
    @Override
    public Optional<User> userForRef(UserRef ref) {
        return Optional.fromNullable(translator.fromDBObject(users.findOne(userRefTranslator.toQuery(ref, "userRef").build())));
    }

    @Override
    public Iterable<User> allUsers() {
        return Iterables.transform(users.find(where().build()), translatorFunction);
    }

    @Override
    public Optional<User> userForId(Long userId) {
        return Optional.fromNullable(translator.fromDBObject(users.findOne(userId)));
    }

    @Override
    public void store(User user) {
        store(translator.toDBObject(user));
    }

    public void store(final DBObject dbo) {
        this.users.update(new BasicDBObject(ID, dbo.get(ID)), dbo, UPSERT, SINGLE);
    }


}
