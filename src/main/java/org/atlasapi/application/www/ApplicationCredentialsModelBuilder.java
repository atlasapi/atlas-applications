package org.atlasapi.application.www;

import org.atlasapi.application.OldApplicationCredentials;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.net.IpRange;

public class ApplicationCredentialsModelBuilder implements ModelBuilder<OldApplicationCredentials> {

	@Override
	public SimpleModel build(OldApplicationCredentials target) {
		SimpleModel model = new SimpleModel();
		
		model.put("apiKey", target.getApiKey());
		model.putStrings("ipRanges", ImmutableList.copyOf(Iterables.transform(target.getIpAddressRanges(), new Function<IpRange, String>(){

			@Override
			public String apply(IpRange range) {
				return range.toFriendlyString();
			}
			
		})));
		
		return model;
	}

}
