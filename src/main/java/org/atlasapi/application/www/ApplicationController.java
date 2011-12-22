package org.atlasapi.application.www;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationManager;
import org.atlasapi.application.users.User;
import org.atlasapi.application.users.UserModelBuilder;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.media.entity.Publisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.ModelListBuilder;
import com.metabroadcast.common.net.IpRange;
import com.metabroadcast.common.social.auth.AuthenticationProvider;

@Controller
public class ApplicationController {

    private static final String APPLICATION_TEMPLATE = "applications/application";
    private static final String APPLICATIONS_INDEX_TEMPLATE = "applications/index";
    private final AuthenticationProvider authProvider;
    private final UserStore userStore;
    private final ApplicationManager manager;

    private ModelListBuilder<Application> modelListBuilder = DelegatingModelListBuilder.delegateTo(new ApplicationModelBuilder());
    private ModelBuilder<Application> modelBuilder = new ApplicationModelBuilder();
    private ModelBuilder<User> userModelBuilder = new UserModelBuilder();

    public ApplicationController(ApplicationManager appManager, AuthenticationProvider authProvider, UserStore userStore) {
        this.manager = appManager;
        this.authProvider = authProvider;
        this.userStore = userStore;
    }

    private Optional<User> user() {
        return userStore.userForRef(authProvider.principal());
    }

    
    private Map<String, Object> standardModel(Map<String, Object> model) {
        model.put("user", userModelBuilder.build(user().get()));
        return model;
    }
    

    public String sendError(HttpServletResponse response, int responseCode) {
        response.setStatus(responseCode);
        response.setContentLength(0);
        return null;
    }

    @RequestMapping(value = "/admin/applications", method = RequestMethod.GET)
    public String applications(Map<String, Object> model) {

        model.put("applications", modelListBuilder.build(manager.applicationsFor(user())));

        return APPLICATIONS_INDEX_TEMPLATE;
    }

    @RequestMapping(value = "/admin/applications", method = RequestMethod.POST)
    public String createApplication(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

        Optional<User> possibleUser = user();

        if (!possibleUser.isPresent()) {
            return sendError(response, HttpServletResponse.SC_FORBIDDEN);
        }

        manager.createNewApplication(possibleUser.get(), request.getParameter("slug"), request.getParameter("title"));

        model.put("applications", modelListBuilder.build(manager.applicationsFor(possibleUser)));
        response.setStatus(HttpServletResponse.SC_OK);

        return APPLICATIONS_INDEX_TEMPLATE;
    }

    @RequestMapping(value = "/admin/applications/{appSlug}", method = RequestMethod.GET)
    public String application(Map<String, Object> model, @PathVariable("appSlug") String slug, HttpServletResponse response) {
        Optional<Application> application = manager.applicationFor(slug);

        if (!application.isPresent()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND);
        }

        standardModel(model).put("application", modelBuilder.build(application.get()));
        return APPLICATION_TEMPLATE;
    }

    @RequestMapping(value="/admin/applications/{appSlug}/publishers/requested", method=RequestMethod.POST)
    public String requestPublisher(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug) {
        
        Publisher publisher = Publisher.fromKey(request.getParameter("pubkey")).valueOrNull();
        if (publisher == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
        }
        
        Application app = manager.requestPublisher(slug, publisher);
        
        model.put("application", modelBuilder.build(app));
        return APPLICATION_TEMPLATE;
    }

    @RequestMapping(value="/admin/applications/{appSlug}/publishers/enabled", method=RequestMethod.POST)
	public String enabledPublisher(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug) {
		
	    Publisher publisher = Publisher.fromKey(request.getParameter("pubkey")).valueOrNull();
	    if (publisher == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
	    }
	    
		Application app = manager.enablePublisher(slug, publisher);
		
		model.put("application", modelBuilder.build(app));
		return APPLICATION_TEMPLATE;
	}

    @RequestMapping(value = "/admin/applications/{appSlug}/publishers/enabled/{pubKey}", method = RequestMethod.DELETE)
    public String disablePublisher(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug, @PathVariable("pubKey") String pubKey) {

        Publisher publisher = Publisher.fromKey(pubKey).valueOrNull();
        if (publisher == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
        }
        
        Application app = manager.disablePublisher(slug, publisher);

        model.put("application", modelBuilder.build(app));
        return APPLICATION_TEMPLATE;
    }

    @RequestMapping(value = "/admin/applications/{appSlug}/precedence", method = RequestMethod.POST)
    public String setPrecedence(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug) {

        Application app = manager.setSourcePrecedence(slug, getPublishersFrom(request.getParameter("precedence")));

        model.put("application", modelBuilder.build(app));
        return APPLICATION_TEMPLATE;
    }

    private List<Publisher> getPublishersFrom(String keyParam) {
        if (keyParam == null) {
            return null;
        }
        return Publisher.fromCsv(keyParam);
    }

    @RequestMapping(value = "/admin/applications/{appSlug}/ipranges", method = RequestMethod.POST)
    public String addIpAddress(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug) {

        IpRange range = IpRange.fromString(Strings.nullToEmpty(request.getParameter("range")));
        if (range == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
        }

        manager.addIpRange(slug, range);

        response.setStatus(HttpServletResponse.SC_OK);
        return "";
    }

    @RequestMapping(value = "/admin/applications/{appSlug}/ipranges/delete", method = RequestMethod.POST)
    public String deleteIpAddress(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("appSlug") String slug) {

        IpRange range = IpRange.fromString(Strings.nullToEmpty(request.getParameter("range")));
        if (range == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
        }

        manager.removeIpRange(slug, range);

        response.setStatus(HttpServletResponse.SC_OK);
        return "";
    }
}
