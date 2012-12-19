package org.atlasapi.application.sources;

import java.math.BigInteger;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationManager;
import org.atlasapi.application.users.Role;
import org.atlasapi.application.users.User;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.application.www.ApplicationModelBuilder;
import org.atlasapi.media.entity.Publisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.social.auth.AuthenticationProvider;
import com.metabroadcast.common.url.Urls;

@Controller
public class SourceController {

    private final ApplicationManager appManager;
    private final AuthenticationProvider authProvider;
    private final UserStore userStore;

    private final SourceIdCodec sourceIdCodec;
    private final SourceModelBuilder sourceModelBuilder;
    private SubstitutionTableNumberCodec idCodec;

    public SourceController(AuthenticationProvider authProvider, ApplicationManager appManager, UserStore userStore) {
        this.authProvider = authProvider;
        this.appManager = appManager;
        this.userStore = userStore;
        this.idCodec = new SubstitutionTableNumberCodec();
        this.sourceIdCodec = new SourceIdCodec(idCodec);
        this.sourceModelBuilder = new SourceModelBuilder(sourceIdCodec);
    }

    private Optional<User> user() {
        return userStore.userForRef(authProvider.principal());
    }
    
    @RequestMapping(value="/admin/sources", method=RequestMethod.GET)
    public String sources(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response) {
        
        Optional<User> possibleUser = user();

        if (possibleUser.isPresent()) {
            User user = possibleUser.get();
            if (user.is(Role.ADMIN)) {
                model.put("sources", SimpleModelList.fromBuilder(sourceModelBuilder, ImmutableSet.copyOf(Publisher.values())));
                return "applications/sources";
            } else {
                return String.format("redirect:/admin/users/%s/sources", idCodec.encode(BigInteger.valueOf(user.getId())));
            }
            
        } else {
            return sendError(response, HttpStatusCode.FORBIDDEN.code());
        }
    }
    
    @RequestMapping(value="/admin/sources/{id}/applications", method=RequestMethod.GET)
    public String applicationsForSource(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
    
        Maybe<Publisher> decodedPublisher = sourceIdCodec.decode(id);
        
        if (decodedPublisher.isNothing()) {
            return sendError(response, HttpStatusCode.NOT_FOUND.code());
        }
        
        Optional<User> possibleUser = user();
        if (!(possibleUser.isPresent() && (possibleUser.get().manages(decodedPublisher) || possibleUser.get().is(Role.ADMIN)))) {
            return sendError(response, HttpStatusCode.FORBIDDEN.code());
        }
        
        Publisher publisher = decodedPublisher.requireValue();
        
        Selection selection = Selection.builder().build(request);
        
        ModelBuilder<Application> applicationModelBuilder = new ApplicationModelBuilder(new SourceSpecificApplicationConfigurationModelBuilder(publisher));
        model.put("applications", SimpleModelList.fromBuilder(applicationModelBuilder , selection.applyTo(appManager.applicationsFor(publisher))));
        model.put("source", sourceModelBuilder.build(publisher));
        
        return "applications/source";
    }
    
    @RequestMapping(value="/admin/sources/{id}/applications/writable", method=RequestMethod.GET)
    public String writableApplicationsForSource(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id, @RequestParam(defaultValue="") final String search) {
    
        Maybe<Publisher> decodedPublisher = sourceIdCodec.decode(id);
        
        if (decodedPublisher.isNothing()) {
            return sendError(response, HttpStatusCode.NOT_FOUND.code());
        }
        
        Optional<User> possibleUser = user();
        if (!(possibleUser.isPresent() && (possibleUser.get().manages(decodedPublisher) || possibleUser.get().is(Role.ADMIN)))) {
            return sendError(response, HttpStatusCode.FORBIDDEN.code());
        }
        
        Publisher publisher = decodedPublisher.requireValue();
        
        Selection selection = Selection.builder().withDefaultLimit(25).withMaxLimit(50).build(request);
        Iterable<Application> apps = ImmutableSet.of();
        // apply filter if specified
        if (search.length() > 1) {
        	apps = appManager.allApplications();
        	apps = Iterables.filter(apps, new Predicate<Application>() {
				@Override
				public boolean apply(@Nullable Application input) {
					return input.getSlug().toLowerCase().contains(search.toLowerCase()) || input.getTitle().toLowerCase().contains(search.toLowerCase()) || input.getCredentials().getApiKey().equals(search);
				}
        	});
        }
        
        ModelBuilder<Application> applicationModelBuilder = new ApplicationModelBuilder(new SourceSpecificApplicationConfigurationModelBuilder(publisher));
        
        model.put("applications", SimpleModelList.fromBuilder(applicationModelBuilder , selection.applyTo(apps)));
        model.put("source", sourceModelBuilder.build(publisher));
        model.put("page", getPagination(request, selection, Iterables.size(apps), search));
        return "applications/writableSources";
    }

    @RequestMapping(value="/admin/sources/{id}/applications/approved", method=RequestMethod.POST)
    public String approveApplication(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
        
        Maybe<Publisher> decodedPublisher = sourceIdCodec.decode(id);
        
        if (decodedPublisher.isNothing()) {
            return sendError(response, HttpStatusCode.NOT_FOUND.code());
        }
        
        Optional<User> possibleUser = user();
        if (!(possibleUser.isPresent() && (possibleUser.get().manages(decodedPublisher) || possibleUser.get().is(Role.ADMIN)))) {
            return sendError(response, HttpStatusCode.FORBIDDEN.code());
        }
        
        Publisher publisher = decodedPublisher.requireValue();
        
        Application application = appManager.approvePublisher(request.getParameter("application"), publisher);

        ModelBuilder<Application> applicationModelBuilder = new ApplicationModelBuilder(new SourceSpecificApplicationConfigurationModelBuilder(publisher));
        model.put("applications", SimpleModelList.fromBuilder(applicationModelBuilder, ImmutableList.of(application)));
        model.put("source", sourceModelBuilder.build(publisher));
        
        return "applications/source";
    }
    
    @RequestMapping(value="/admin/sources/{id}/writable/applications/add", method=RequestMethod.POST)
    public String addWritableApplication(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
        
        Maybe<Publisher> decodedPublisher = sourceIdCodec.decode(id);
        
        if (decodedPublisher.isNothing()) {
            return sendError(response, HttpStatusCode.NOT_FOUND.code());
        }
        
        Optional<User> possibleUser = user();
        if (!(possibleUser.isPresent() && (possibleUser.get().manages(decodedPublisher) || possibleUser.get().is(Role.ADMIN)))) {
            return sendError(response, HttpStatusCode.FORBIDDEN.code());
        }
        
        Publisher publisher = decodedPublisher.requireValue();

        Application application = appManager.addWritableSource(request.getParameter("application"), publisher);

        ModelBuilder<Application> applicationModelBuilder = new ApplicationModelBuilder(new SourceSpecificApplicationConfigurationModelBuilder(publisher));
        model.put("applications", SimpleModelList.fromBuilder(applicationModelBuilder, ImmutableList.of(application)));
        model.put("source", sourceModelBuilder.build(publisher));
        
        return "applications/source";
    }
    
    @RequestMapping(value="/admin/sources/{id}/writable/applications/remove", method=RequestMethod.POST)
    public String removeWritableApplication(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
        
        Maybe<Publisher> decodedPublisher = sourceIdCodec.decode(id);
        
        if (decodedPublisher.isNothing()) {
            return sendError(response, HttpStatusCode.NOT_FOUND.code());
        }
        
        Optional<User> possibleUser = user();
        if (!(possibleUser.isPresent() && (possibleUser.get().manages(decodedPublisher) || possibleUser.get().is(Role.ADMIN)))) {
            return sendError(response, HttpStatusCode.FORBIDDEN.code());
        }
        
        Publisher publisher = decodedPublisher.requireValue();
        
        Application application = appManager.removeWritableSource(request.getParameter("application"), publisher);

        ModelBuilder<Application> applicationModelBuilder = new ApplicationModelBuilder(new SourceSpecificApplicationConfigurationModelBuilder(publisher));
        model.put("applications", SimpleModelList.fromBuilder(applicationModelBuilder, ImmutableList.of(application)));
        model.put("source", sourceModelBuilder.build(publisher));
        
        return "applications/source";
    }
    
    public SimpleModel getPagination(HttpServletRequest request, Selection selection, int max, String search) {
    	// build page model for prev/next buttons
        SimpleModel page = new SimpleModel();
        page.put("limit", selection.getLimit());
        page.put("offset", selection.getOffset());
        page.put("max", max);
        page.put("search", search);
        String url = request.getRequestURI();
        if (search.length() > 1) {
        	url = Urls.appendParameters(url, "search", search);
        }
        if (selection.hasNonZeroOffset()) {
        	int offset = selection.getOffset() - selection.getLimit();
        	if (offset < 0) {
        		offset = 0;
        	}
            Selection prev = selection.withOffset(offset);
            page.put("prevUrl", prev.appendToUrl(url));
        }
        
        if ((selection.getOffset() + selection.getLimit()) < max) {
            Selection next = selection.withOffsetPlus(selection.getLimit());
            page.put("nextUrl", next.appendToUrl(url));
        }
        return page;
    }

    public String sendError(HttpServletResponse response, final int code) {
        response.setStatus(code);
        response.setContentLength(0);
        return null;
    }
    
}
