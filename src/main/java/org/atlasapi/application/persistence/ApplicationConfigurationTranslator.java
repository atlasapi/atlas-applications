package org.atlasapi.application.persistence;

import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ApplicationConfigurationTranslator {

	private static final String EXCLUDED_PUBLISHERS_KEY = "excludedPublishers";
	private static final String INCLUDED_PUBLISHERS_KEY = "includedPublishers";

	public DBObject toDBObject(ApplicationConfiguration configuration) {
		BasicDBObject dbo = new BasicDBObject();
		
		TranslatorUtils.fromSet(dbo, publisherKeySetOf(configuration.getIncludedPublishers()), INCLUDED_PUBLISHERS_KEY);
		TranslatorUtils.fromSet(dbo, publisherKeySetOf(configuration.getExcludedPublishers()), EXCLUDED_PUBLISHERS_KEY);
		
		return dbo;
	}
	
	private Set<String> publisherKeySetOf(Set<Publisher> publishers) {
		return ImmutableSet.copyOf(Iterables.transform(publishers, new Function<Publisher, String>(){

			@Override
			public String apply(Publisher publisher) {
				return publisher.key();
			}
			
		}));
	}

	public ApplicationConfiguration fromDBObject(DBObject dbo) {
		ApplicationConfiguration configuration = new ApplicationConfiguration();
		
		configuration.setIncludedPublishers(publisherSetOf(TranslatorUtils.toSet(dbo, INCLUDED_PUBLISHERS_KEY)));
		configuration.setExcludedPublishers(publisherSetOf(TranslatorUtils.toSet(dbo, EXCLUDED_PUBLISHERS_KEY)));
		
		return configuration;
	}
	
	private Set<Publisher> publisherSetOf(Set<String> keys) {
		return ImmutableSet.copyOf(Iterables.transform(keys, new Function<String, Publisher>(){

			@Override
			public Publisher apply(String key) {
				Maybe<Publisher> publisher = Publisher.fromKey(key);
				return publisher.valueOrNull();
			}
			
		}));
	}

}
