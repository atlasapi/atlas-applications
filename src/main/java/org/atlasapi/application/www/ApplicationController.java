package org.atlasapi.application.www;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.ApplicationCredentials;
import org.atlasapi.application.persistence.ApplicationPersistor;
import org.atlasapi.application.persistence.ApplicationReader;
import org.atlasapi.media.entity.Publisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.ModelListBuilder;
import com.metabroadcast.common.net.IpRange;

@Controller
public class ApplicationController {
	
	private final ApplicationReader reader;
	private final ApplicationPersistor persistor;
	
	private ModelListBuilder<Application> modelListBuilder = DelegatingModelListBuilder.delegateTo(new ApplicationModelBuilder());
	private ModelBuilder<Application> modelBuilder = new ApplicationModelBuilder();

	public ApplicationController(ApplicationReader reader, ApplicationPersistor persistor){
		this.reader = reader;
		this.persistor = persistor;
	}
    
    @RequestMapping(value="/admin/applications", method=RequestMethod.GET)
    public String applications(Map<String, Object> model) {
    	
    	model.put("applications", modelListBuilder.build(reader.applications()));
    	
        return "applications/index";
    }
    
    @RequestMapping(value="/admin/applications", method=RequestMethod.POST)
    public String createApplication(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
    	String slug = request.getParameter("slug");
    	String title= request.getParameter("title");
    	
    	if (slug == null || slug.matches("[a-z0-9][a-z0-9\\-]{1,255}") ||title == null) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		response.setContentLength(0);
    		return null;
    	}
    	
    	Application newApp = createNewApplication(slug, title);
    	
    	try {
    		persistor.persist(newApp);
    	
    		model.put("applications", modelListBuilder.build(reader.applications()));
    		response.setStatus(HttpServletResponse.SC_OK);
    	
    		return "applications/index";
    	} catch (IllegalArgumentException iae) {
    		response.setStatus(HttpServletResponse.SC_CONFLICT);
    		response.setContentLength(0);
    		return null;
    	}
    }
    
    @RequestMapping(value="/admin/applications/{appSlug}", method=RequestMethod.GET)
    public String application(Map<String, Object> model, @PathVariable("appSlug") String slug, HttpServletResponse response) {
    	Application application = reader.applicationFor(slug);
    	
    	if (application == null) {
    		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		response.setContentLength(0);
    		return null;
    	}
    	
    	model.put("application", modelBuilder.build(application));
    	
    	return "applications/application";
    }
    
	private Application createNewApplication(String slug, String title) {
    	Application application = new Application(slug);
    	application.setTitle(title);
    	
    	ApplicationCredentials credentials = new ApplicationCredentials();
    	String apiKey = UUID.randomUUID().toString().replaceAll("-", "");
    	credentials.setApiKey(apiKey);
    	
    	application.setCredentials(credentials);
    	
    	ApplicationConfiguration config = ApplicationConfiguration.defaultConfiguration();
    	
    	application.setConfiguration(config);
    	
    	return application;
    }
	
	@RequestMapping(value="/admin/applications/{appSlug}/publishers", method=RequestMethod.POST)
	public String addPublisher(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug) {
		Application app = reader.applicationFor(slug);
		if (app == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		response.setContentLength(0);
			return null;
		}

		Publisher publisher = getPublisherFrom(request.getParameter("pubkey"));
		if (publisher == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		response.setContentLength(0);
			return null;
		}
		
		Set<Publisher> currentPublishers = app.getConfiguration().getIncludedPublishers();
		app.getConfiguration().setIncludedPublishers(Iterables.concat(currentPublishers, ImmutableSet.of(publisher)));
		persistor.update(app);
		
		model.put("application", modelBuilder.build(app));
		
		return "applications/application";
	}
	
	@RequestMapping(value="/admin/applications/{appSlug}/publishers/{pubKey}", method=RequestMethod.DELETE)
	public String removePublisher(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug, @PathVariable("pubKey") String pubKey) {
		Application app = reader.applicationFor(slug);
		if (app == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		response.setContentLength(0);
			return null;
		}
		
		Publisher publisher = getPublisherFrom(pubKey);
		if (publisher == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		response.setContentLength(0);
			return null;
		}
		
		Set<Publisher> newPublishers = Sets.newHashSet(app.getConfiguration().getIncludedPublishers());
		newPublishers.remove(publisher);
		app.getConfiguration().setIncludedPublishers(newPublishers);
		persistor.update(app);
		
		model.put("application", modelBuilder.build(app));
		
		return "applications/application";
	}
	
	private Publisher getPublisherFrom(String keyParam) {
		if (keyParam == null){
			return null;
		}
		return Publisher.fromKey(keyParam).valueOrNull();
	}
	
	@RequestMapping(value="/admin/applications/{appSlug}/ipranges", method=RequestMethod.POST)
	public String addIpAddress(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug){
		Application app = reader.applicationFor(slug);
		if (app == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		response.setContentLength(0);
			return null;
		}
		
		String addressString = request.getParameter("ipaddress");
		IpRange range;
		if (addressString == null || (range = IpRange.fromString(addressString)) == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		response.setContentLength(0);
			return null;
		}

		Set<IpRange> currentIps = app.getCredentials().getIpAddressRanges();
		app.getCredentials().setIpAddresses(Iterables.concat(currentIps, ImmutableList.of(range)));
		persistor.update(app);
		
		response.setStatus(HttpServletResponse.SC_OK);
		return "";
	}
	
	@RequestMapping(value="/admin/applications/{appSlug}/ipranges/delete", method=RequestMethod.POST)
	public String deleteIpAddress(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug) {
		Application app = reader.applicationFor(slug);
		if (app == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		response.setContentLength(0);
			return null;
		}
		
		String rangeString = request.getParameter("range");
		if (rangeString == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		response.setContentLength(0);
			return null;
		}
		
		IpRange range = IpRange.fromString(rangeString);
		if (range == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		response.setContentLength(0);
			return null;
		}
		
		Set<IpRange> currentRanges = app.getCredentials().getIpAddressRanges();
		
		if (!currentRanges.contains(range)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		response.setContentLength(0);
		}
		
		Set<IpRange> newIps = Sets.newHashSet(currentRanges);
		newIps.remove(range);
		
		app.getCredentials().setIpAddresses(newIps);
		persistor.update(app);
		
		response.setStatus(HttpServletResponse.SC_OK);
		return "";
	}
}
