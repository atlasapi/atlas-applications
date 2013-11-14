package org.atlasapi.application.users;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationStore;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.application.sources.SourceModelBuilder;
import org.atlasapi.application.users.v3.User;
import org.atlasapi.application.v3.Application;
import org.atlasapi.application.www.ApplicationModelBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import com.metabroadcast.common.social.auth.AuthenticationProvider;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.url.Urls;

@Controller
public class UserController {

    private final AuthenticationProvider authProvider;
    private final UserStore userStore;
    private final NumberToShortStringCodec idCodec;
    private final ApplicationStore appStore;
    private final ApplicationModelBuilder appModelBuilder;
    private final UserModelBuilder userModelBuilder;
    private final SourceModelBuilder sourceModelBuilder;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private SelectionBuilder selectionBuilder = Selection.builder().withDefaultLimit(DEFAULT_PAGE_SIZE).withMaxLimit(50);

    public UserController(AuthenticationProvider authProvider, UserStore userStore, ApplicationStore appStore) {
        this.authProvider = authProvider;
        this.userStore = userStore;
        this.appStore = appStore;
        this.idCodec = new SubstitutionTableNumberCodec();
        this.appModelBuilder = new ApplicationModelBuilder();
        this.userModelBuilder = new UserModelBuilder();
        this.sourceModelBuilder = new SourceModelBuilder(new SourceIdCodec(idCodec));
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    public String listAllUsers(HttpServletRequest request,
                               HttpServletResponse response, 
                               Map<String, Object> model,
                               @RequestParam(defaultValue="") final String search) {
        
        UserRef principal = authProvider.principal();

        Optional<User> existingUser = userStore.userForRef(principal);
        
        if (!existingUser.isPresent() || !existingUser.get().is(Role.ADMIN)) {
            response.setStatus(HttpStatusCode.FORBIDDEN.code());
            response.setContentLength(0);
            return "";
        }
        
        Iterable<User> users = userStore.allUsers();
        
        Selection selection = selectionBuilder.build(request);
        // apply filter if specified
        if (search.length() > 1) {
            users = Iterables.filter(users, new Predicate<User>() {
                @Override
                public boolean apply(@Nullable User input) {
                    return contains(input.getFullName(), search) 
                            || contains(input.getCompany(), search) 
                            || contains(input.getEmail(), search)
                            || contains(input.getWebsite(), search);
                }
            });
        }

        model.put("users", SimpleModelList.fromBuilder(userModelBuilder, selection.applyTo(users)));
        model.put("page", getPagination(request, selection, Iterables.size(users), search));
        return "users/list";
    }
    
    private boolean contains(String target, String search) {
        return target != null && target.toLowerCase().contains(search.toLowerCase());
    }
    
    @RequestMapping(value = "/admin/users/{id}/account", method = RequestMethod.GET)
    public String showUserProfileForm(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Map<String, Object> model, 
                                      @PathVariable("id") String id,
                                      @RequestParam(defaultValue = "") String redirectUri) {

        Optional<User> existingUser = userStore.userForId(idCodec.decode(id).longValue());

        if (!existingUser.isPresent()) {
            response.setStatus(HttpStatusCode.NOT_FOUND.code());
            response.setContentLength(0);
            return null;
        }

        UserRef principal = authProvider.principal();
        User user = existingUser.get();

        //This is the not logged-in user's page or the logged-in user is not an administrator.
        if (!(user.getUserRef().equals(principal) || userStore.userForRef(principal).get().is(Role.ADMIN))) {
            response.setStatus(HttpStatusCode.FORBIDDEN.code());
            response.setContentLength(0);
            return null;
        }
        model.put("user", userModelBuilder.build(user));
        if (redirectUri.isEmpty()) {
            model.put("redirectUri", "/admin/users/" + id + "/account");
        } else {
            model.put("redirectUri", redirectUri);
        }
        return "users/account";
    }
    
    @RequestMapping(value = "/admin/users/{id}/account", method = RequestMethod.POST)
    public View updateUserProfile(Map<String, Object> model, 
            HttpServletRequest request,
            HttpServletResponse response, 
            @PathVariable("id") String id,
            @RequestParam("fullName") String fullName,
            @RequestParam("website") String website,
            @RequestParam("email") String email,
            @RequestParam("company") String company,
            @RequestParam("redirectUri") String redirectUri) {
        Optional<User> existingUser = userStore.userForId(idCodec.decode(id).longValue());
        if (!existingUser.isPresent()) {
            response.setStatus(HttpStatusCode.NOT_FOUND.code());
            response.setContentLength(0);
            return null;
        }
        UserRef principal = authProvider.principal();
        User user = existingUser.get();
        //This is the not logged-in user's page or the logged-in user is not an administrator.
        if (!(user.getUserRef().equals(principal) || userStore.userForRef(principal).get().is(Role.ADMIN))) {
            response.setStatus(HttpStatusCode.FORBIDDEN.code());
            response.setContentLength(0);
            return null;
        }
        user.setFullName(fullName);
        user.setWebsite(website);
        user.setEmail(email);
        user.setCompany(company);
        userStore.store(user);
        return new RedirectView(redirectUri);
    }

    @RequestMapping(value = "/admin/users/{id}/applications", method = RequestMethod.GET)
    public String showUserApplications(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model, @PathVariable("id") String id, @RequestParam(defaultValue="") final String search) throws IOException {
        Optional<User> existingUser = userStore.userForId(idCodec.decode(id).longValue());

        if (!existingUser.isPresent()) {
            response.setStatus(HttpStatusCode.NOT_FOUND.code());
            response.setContentLength(0);
            return null;
        }

        UserRef principal = authProvider.principal();
        User user = existingUser.get();

        //This is the not logged-in user's page or the logged-in user is not an administrator.
        if (!(user.getUserRef().equals(principal) || userStore.userForRef(principal).get().is(Role.ADMIN))) {
            response.setStatus(HttpStatusCode.FORBIDDEN.code());
            response.setContentLength(0);
            return null;
        }
        Iterable<Application> apps = appStore.applicationsFor(existingUser);
        // apply filter if specified
        if (search.length() > 1) {
        	apps = Iterables.filter(apps, new Predicate<Application>() {
				@Override
				public boolean apply(@Nullable Application input) {
					return input.getSlug().contains(search.toLowerCase().toLowerCase()) || input.getTitle().toLowerCase().contains(search.toLowerCase()) || input.getCredentials().getApiKey().equals(search);
				}
        	});
        }
        Selection selection = selectionBuilder.build(request);

        model.put("applications", SimpleModelList.fromBuilder(appModelBuilder, selection.applyTo(apps)));
        model.put("page", getPagination(request, selection, Iterables.size(apps), search));
        return "applications/index";
    }
    
    @RequestMapping(value = "/admin/users/{id}/sources", method = RequestMethod.GET)
    public String showUserSources(HttpServletResponse response, Map<String, Object> model, @PathVariable("id") String id) throws IOException {
        
        Optional<User> existingUser = userStore.userForId(idCodec.decode(id).longValue());

        if (!existingUser.isPresent()) {
            response.setStatus(HttpStatusCode.NOT_FOUND.code());
            response.setContentLength(0);
            return null;
        }

        UserRef principal = authProvider.principal();
        User user = existingUser.get();

        if (!user.getUserRef().equals(principal) /* && !isAdmin(principal) */) {
            response.setStatus(HttpStatusCode.FORBIDDEN.code());
            response.setContentLength(0);
            return null;
        }

        model.put("sources", SimpleModelList.fromBuilder(sourceModelBuilder, user.getSources()));

        return "applications/sources";
        
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
        // UrlEncoding.encode(accessToken)
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
}
