package org.atlasapi.application.users;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationStore;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.application.sources.SourceModelBuilder;
import org.atlasapi.application.www.ApplicationModelBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Optional;
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

@Controller
public class UserController {

    private final AuthenticationProvider authProvider;
    private final UserStore userStore;
    private final NumberToShortStringCodec idCodec;
    private final ApplicationStore appStore;
    private final ApplicationModelBuilder appModelBuilder;
    private final SourceModelBuilder sourceModelBuilder;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private SelectionBuilder selectionBuilder = Selection.builder().withDefaultLimit(DEFAULT_PAGE_SIZE).withMaxLimit(50);

    public UserController(AuthenticationProvider authProvider, UserStore userStore, ApplicationStore appStore) {
        this.authProvider = authProvider;
        this.userStore = userStore;
        this.appStore = appStore;
        this.idCodec = new SubstitutionTableNumberCodec();
        this.appModelBuilder = new ApplicationModelBuilder();
        this.sourceModelBuilder = new SourceModelBuilder(new SourceIdCodec(idCodec));
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    public String forwardToUser(HttpServletResponse response) {
        UserRef principal = authProvider.principal();

        Optional<User> existingUser = userStore.userForRef(principal);
        
        if (!existingUser.isPresent() || !existingUser.get().is(Role.ADMIN)) {
            response.setStatus(HttpStatusCode.FORBIDDEN.code());
            response.setContentLength(0);
            return "";
        }
        
        response.setStatus(HttpStatusCode.SERVICE_UNAVAILABLE.code());
        response.setContentLength(0);
        
        return "";//"applications/users";
    }

    @RequestMapping(value = "/admin/users/{id}/applications", method = RequestMethod.GET)
    public String showUserApplications(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model, @PathVariable("id") String id) throws IOException {

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

        Selection selection = selectionBuilder.build(request);
        Iterable<Application> apps = appStore.applicationsFor(existingUser);

        model.put("applications", SimpleModelList.fromBuilder(appModelBuilder, selection.applyTo(apps)));
        model.put("page", getPagination(request, selection, Iterables.size(apps)));
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
    
    public SimpleModel getPagination(HttpServletRequest request, Selection selection, int max) {
    	// build page model for prev/next buttons
        SimpleModel page = new SimpleModel();
        page.put("limit", selection.getLimit());
        page.put("offset", selection.getOffset());
        page.put("max", max);
        if (selection.getOffset() > 0) {
        	int offset = selection.getOffset() - selection.getLimit();
        	if (offset < 0) {
        		offset = 0;
        	}
            Selection prev = selection.withOffset(offset);
            page.put("prevUrl", prev.appendToUrl(request.getRequestURI()));
        }
        if ((selection.getOffset() + selection.getLimit()) < max) {
            Selection next = selection.withOffsetPlus(selection.getLimit());
            page.put("nextUrl", next.appendToUrl(request.getRequestURI()));
        }
        return page;
    }
}
