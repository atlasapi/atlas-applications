package org.atlasapi.application.www;

import java.net.InetAddress;

import org.atlasapi.application.ApplicationCredentials;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;

public class ApplicationCredentialsModelBuilder implements ModelBuilder<ApplicationCredentials> {

	@Override
	public SimpleModel build(ApplicationCredentials target) {
		SimpleModel model = new SimpleModel();
		
		model.put("apiKey", target.getApiKey());
		model.put("ipAddresses", ImmutableList.copyOf(Iterables.transform(target.getIpAddresses(), new Function<InetAddress, SimpleModel>(){

			@Override
			public SimpleModel apply(InetAddress address) {
				return new SimpleModel().put("ipAddress", address.getHostAddress());
			}
			
		})));
		
		return model;
	}

}
