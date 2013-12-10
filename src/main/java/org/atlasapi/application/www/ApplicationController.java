package org.atlasapi.application.www;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlas.application.notification.EmailNotificationSender;
import org.atlasapi.application.sources.SourceRequest;
import org.atlasapi.application.sources.SourceRequestStore;
import org.atlasapi.application.sources.UsageType;
import org.atlasapi.application.users.v3.Role;
import org.atlasapi.application.users.v3.User;
import org.atlasapi.application.users.v3.UserModelBuilder;
import org.atlasapi.application.users.v3.UserStore;
import org.atlasapi.application.v3.Application;
import org.atlasapi.application.v3.ApplicationManager;
import org.atlasapi.media.entity.Publisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.ModelListBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.net.IpRange;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import com.metabroadcast.common.social.auth.AuthenticationProvider;
import com.metabroadcast.common.url.Urls;

@Controller
public class ApplicationController {

    private static final String APPLICATION_TEMPLATE = "applications/application";
    private static final String APPLICATIONS_INDEX_TEMPLATE = "applications/index";
    private static final int DEFAULT_PAGE_SIZE = 15;

    private final AuthenticationProvider authProvider;
    private final UserStore userStore;
    private final SourceRequestStore sourceRequestStore;
    private final ApplicationManager manager;
    private final EmailNotificationSender emailSender;

    private final ModelListBuilder<Application> modelListBuilder = DelegatingModelListBuilder.delegateTo(new ApplicationModelBuilder());
    private final ModelBuilder<Application> modelBuilder = new ApplicationModelBuilder();
    private final ModelBuilder<User> userModelBuilder = new UserModelBuilder();
    private final SelectionBuilder selectionBuilder = Selection.builder()
            .withDefaultLimit(DEFAULT_PAGE_SIZE)
            .withMaxLimit(50);
    private final Gson publisherKeyDeserializer = new GsonBuilder()
            .registerTypeAdapter(Publisher.class, new PublisherKeyDeserializer())
            .create();

    public ApplicationController(ApplicationManager appManager,
            AuthenticationProvider authProvider, 
            UserStore userStore,
            SourceRequestStore sourceRequestStore,
            EmailNotificationSender emailSender) {
        this.manager = appManager;
        this.authProvider = authProvider;
        this.userStore = userStore;
        this.sourceRequestStore = sourceRequestStore;
        this.emailSender = emailSender;
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
    public String applications(Map<String, Object> model, HttpServletRequest request,
            @RequestParam(defaultValue = "") final String search) {

        Selection selection = selectionBuilder.build(request);
        Optional<User> user = user();
        Iterable<Application> apps = null;

        if (user.isPresent() && user.get().is(Role.ADMIN)) {
            apps = manager.allApplications();
        } else {
            apps = manager.applicationsFor(user);
        }

        // apply filter if specified
        if (search.length() > 1) {
            apps = Iterables.filter(apps, new Predicate<Application>() {

                @Override
                public boolean apply(@Nullable Application input) {
                    return input.getSlug().toLowerCase().contains(search.toLowerCase())
                        || input.getTitle().toLowerCase().contains(search.toLowerCase())
                        || input.getCredentials().getApiKey().equals(search);
                }
            });
        }

        model.put("applications", modelListBuilder.build(selection.applyTo(apps)));
        model.put("page", getPagination(request, selection, Iterables.size(apps), search));

        return APPLICATIONS_INDEX_TEMPLATE;
    }

    public SimpleModel getPagination(HttpServletRequest request, Selection selection, int max,
            String search) {
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

    @RequestMapping(value = "/admin/applications", method = RequestMethod.POST)
    public String createApplication(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) {

        Optional<User> possibleUser = user();

        if (!possibleUser.isPresent()) {
            return sendError(response, HttpServletResponse.SC_FORBIDDEN);
        }

        manager.createNewApplication(possibleUser.get(),
                request.getParameter("slug"),
                request.getParameter("title"));

        model.put("applications", modelListBuilder.build(manager.applicationsFor(possibleUser)));
        response.setStatus(HttpServletResponse.SC_OK);

        return APPLICATIONS_INDEX_TEMPLATE;
    }

    @RequestMapping(value = "/admin/applications/{appSlug}", method = RequestMethod.GET)
    public String application(Map<String, Object> model, @PathVariable("appSlug") String slug,
            HttpServletResponse response) {
        Optional<Application> application = manager.applicationFor(slug);

        if (!application.isPresent()) {
            return sendError(response, HttpServletResponse.SC_NOT_FOUND);
        }

        standardModel(model).put("application", modelBuilder.build(application.get()));
        return APPLICATION_TEMPLATE;
    }

    @RequestMapping(value = "/admin/applications/{appSlug}/publishers/requested", method = RequestMethod.POST)
    public String requestPublisher(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response, @PathVariable("appSlug") String slug,
            @RequestParam(defaultValue = "") String reason,
            @RequestParam(defaultValue = "") String appUrl) throws UnsupportedEncodingException,
            MessagingException {
        
        UsageType usageType = UsageType.valueOf(request.getParameter("usageType").toUpperCase());
        
        Publisher publisher = Publisher.fromKey(request.getParameter("pubkey")).valueOrNull();
        if (publisher == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
        }

        Application app = manager.requestPublisher(slug, publisher);
        Optional<User> user = user();
        model.put("application", modelBuilder.build(app));
        SourceRequest sourceRequest = SourceRequest.builder()
                .withAppSlug(app.getSlug())
                .withEmail(user.get().getEmail())
                .withPublisher(publisher)
                .withReason(reason)
                .withUsageType(usageType)
                .withAppUrl(appUrl)
                .withApproved(false)
                .build();
        
        sourceRequestStore.store(sourceRequest);

        // send notification of request
        emailSender.sendNotificationOfPublisherRequestToAdmin(app, sourceRequest);
        if (sourceRequest.getEmail() != null) {
            emailSender.sendNotificationOfPublisherRequestToUser(app, sourceRequest);
        }

        return APPLICATION_TEMPLATE;
    }

    public static class PublisherKeyDeserializer implements JsonDeserializer<Publisher> {

        @Override
        public Publisher deserialize(JsonElement json, Type type,
                JsonDeserializationContext context) throws JsonParseException {
            String pubKey = json.getAsJsonPrimitive().getAsString();
            if (Publisher.fromKey(pubKey).hasValue()) {
                return Publisher.fromKey(pubKey).requireValue();
            } else {
                throw new JsonParseException("Cannot parse " + pubKey);
            }
        }

    }

    @RequestMapping(value = "/admin/applications/{appSlug}/publishers", method = RequestMethod.POST)
    public String changePublisherConfiguration(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response,
            @PathVariable("appSlug") String slug) throws IOException {
        Reader reader = new InputStreamReader(request.getInputStream());
        PublisherConfiguration configuration = publisherKeyDeserializer.fromJson(reader,
                PublisherConfiguration.class);
        Application app = manager.setPublisherConfiguration(slug, configuration);
        model.put("application", modelBuilder.build(app));
        return APPLICATION_TEMPLATE;
    }

    @RequestMapping(value = "/admin/applications/{appSlug}/publishers/enabled", method = RequestMethod.POST)
    public String enabledPublisher(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response, @PathVariable("appSlug") String slug) {

        Publisher publisher = Publisher.fromKey(request.getParameter("pubkey")).valueOrNull();
        if (publisher == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
        }

        Application app = manager.enablePublisher(slug, publisher);

        model.put("application", modelBuilder.build(app));
        return APPLICATION_TEMPLATE;
    }

    @RequestMapping(value = "/admin/applications/{appSlug}/publishers/enabled/{pubKey}", method = RequestMethod.DELETE)
    public String disablePublisher(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response, @PathVariable("appSlug") String slug,
            @PathVariable("pubKey") String pubKey) {

        Publisher publisher = Publisher.fromKey(pubKey).valueOrNull();
        if (publisher == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
        }

        Application app = manager.disablePublisher(slug, publisher);

        model.put("application", modelBuilder.build(app));
        return APPLICATION_TEMPLATE;
    }

    @RequestMapping(value = "/admin/applications/{appSlug}/precedence", method = RequestMethod.POST)
    public String setPrecedence(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response, @PathVariable("appSlug") String slug) {

        Application app = manager.setSourcePrecedence(slug,
                getPublishersFrom(request.getParameter("precedence")));

        model.put("application", modelBuilder.build(app));
        return APPLICATION_TEMPLATE;
    }

    @RequestMapping(value = "/admin/applications/{appSlug}/precedenceOff", method = RequestMethod.POST)
    public String setPrecedenceOff(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response, @PathVariable("appSlug") String slug) {

        Application app = manager.setSourcePrecedence(slug, null);

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
    public String addIpAddress(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response, @PathVariable("appSlug") String slug) {

        IpRange range = IpRange.fromString(Strings.nullToEmpty(request.getParameter("range")));
        if (range == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
        }

        manager.addIpRange(slug, range);

        response.setStatus(HttpServletResponse.SC_OK);
        return "";
    }

    @RequestMapping(value = "/admin/applications/{appSlug}/ipranges/delete", method = RequestMethod.POST)
    public String deleteIpAddress(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response, @PathVariable("appSlug") String slug) {

        IpRange range = IpRange.fromString(Strings.nullToEmpty(request.getParameter("range")));
        if (range == null) {
            return sendError(response, HttpServletResponse.SC_BAD_REQUEST);
        }

        manager.removeIpRange(slug, range);

        response.setStatus(HttpServletResponse.SC_OK);
        return "";
    }

    @RequestMapping(value = "/admin")
    public View logout(HttpServletResponse response) {

        return new RedirectView("/admin/applications");
    }
}
