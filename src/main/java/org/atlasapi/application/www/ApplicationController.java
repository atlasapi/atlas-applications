package org.atlasapi.application.www;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.ApplicationCredentials;
import org.atlasapi.application.persistence.ApplicationPersistor;
import org.atlasapi.application.persistence.ApplicationReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.ModelListBuilder;

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
    	
    	Application newApp = createNewApplication(slug, title);
    	
    	persistor.persist(newApp);
    	
    	model.put("applications", modelListBuilder.build(reader.applications()));
    	response.setStatus(HttpServletResponse.SC_OK);
    	
    	return "applications/index";
    }
    
    @RequestMapping(value="/admin/applications/{appSlug}", method=RequestMethod.GET)
    public String application(Map<String, Object> model, @PathVariable("appSlug") String slug, HttpServletResponse response) {
    	Application application = reader.applicationFor(slug);
    	
    	if (application == null) {
    		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
}
