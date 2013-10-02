package org.atlasapi.application.auth;

import static org.atlasapi.application.users.Role.ADMIN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.application.users.User;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.media.entity.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.social.auth.RequestScopedAuthenticationProvider;
import com.metabroadcast.common.social.auth.UserNotLoggedInException;

/**
 * {@link HandlerInterceptor} that checks whether the use is not logged in
 * before allowing them access to protected pages.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class AdminAuthenticationInterceptor extends HandlerInterceptorAdapter {

    private static final Log LOG = LogFactory.getLog(AdminAuthenticationInterceptor.class);

    private ViewResolver viewResolver;
    private RequestScopedAuthenticationProvider authService;

    private Map<String, List<String>> authenticationRequiredByMethod = initAuthMap();
    private Set<String> exceptions = ImmutableSet.of();
    
    private UserNotAuthenticatedHandler userNotAuthenticatedHandler = new DefaultUserNotAuthenticatedHandler();
    private String loginView;
    
    private UserStore userStore;

    private final Pattern applicationPagePattern = Pattern.compile("/admin/applications/([a-z0-9][a-z0-9\\-]{1,255})/?.*");
    private final Pattern sourcePagePattern = Pattern.compile("/admin/sources/([a-z0-9][a-z0-9]{1,255})/?.*");
    private final SourceIdCodec sourceIdCodec = new SourceIdCodec();

    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (authenticationIsRequired(request)) {
            authService.init(request, response);
            
            if (!authService.isAuthenticated()) {
                return userNotAuthenticatedHandler.userNotAuthenticated(request, response);
            }
            
            final User user = user();
            
            if (!user.is(ADMIN) && (isApplicationPage(request) && !user.manages(applicationSlug(request))
                                ||  isSourcePage(request) && !user.manages(sourceFrom(request)))) {
                response.setStatus(HttpStatusCode.FORBIDDEN.code());
                response.setContentLength(0);
                return false;
            }
        }
        
        return true;
    }

    private Optional<Publisher> sourceFrom(HttpServletRequest request) {
        final Matcher matcher = applicationPagePattern.matcher(request.getRequestURI());
        if (matcher.matches()) {
            return sourceIdCodec.decode(matcher.group(1));
        }
        throw new IllegalArgumentException();
    }
    
    private boolean isSourcePage(HttpServletRequest request) {
        return sourcePagePattern.matcher(request.getRequestURI()).matches();
    }

    private String applicationSlug(HttpServletRequest request) {
        final Matcher matcher = applicationPagePattern.matcher(request.getRequestURI());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException();
    }

    private boolean isApplicationPage(HttpServletRequest request) {
        return applicationPagePattern.matcher(request.getRequestURI()).matches();
    }

    public User user() {
        return userStore.userForRef(authService.principal()).get();
    }
    
    public void setUserStore(UserStore userStore) {
        this.userStore = userStore;
    }
    
    private boolean authenticationIsRequired(final HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        List<String> uris = authenticationRequiredByMethod.get(request.getMethod());

        if (uris == null) {
            LOG.info("No matching auth-required map for request method: " + request.getMethod());
            return false; // given we are aren't protecting things that are really private.
        }

        for (String uri : uris) {
            if (requestUri.startsWith(uri)) {
                return !exceptions.contains(requestUri);
            }
        }

        return false;
    }

    public void setAuthenticationRequired(List<String> uris) {
        List<String> getList = authenticationRequiredByMethod.get("GET");
        List<String> postList = authenticationRequiredByMethod.get("POST");

        for (String uri : uris) {
            if (uri.startsWith("GET:")) {
                getList.add(uri.substring(4));
            } else if (uri.startsWith("POST")) {
                postList.add(uri.substring(5));
            } else {
                getList.add(uri);
                postList.add(uri);
            }
        }
    }

    private Map<String, List<String>> initAuthMap() {
        Map<String, List<String>> authMap = Maps.newHashMap();
        ArrayList<String> getList = Lists.newArrayList();
        ArrayList<String> postList = Lists.newArrayList();

        authMap.put("GET", getList);
        authMap.put("get", getList);
        authMap.put("POST", postList);
        authMap.put("post", postList);

        return authMap;
    }

    public void setExceptions(Iterable<String> uris) {
        this.exceptions = ImmutableSet.copyOf(uris);
    }

    public void setLoginView(String loginPage) {
        this.loginView = loginPage;
    }

    @Autowired
    public void setAuthService(RequestScopedAuthenticationProvider authService) {
        this.authService = authService;
    }

    public void setAuthenticationRequiredByMethod(Map<String, List<String>> authenticationRequiredByMethod) {
        this.authenticationRequiredByMethod = authenticationRequiredByMethod;
    }
    
    @Autowired
    public void setViewResolver(ViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }
    
    public interface UserNotAuthenticatedHandler {
        boolean userNotAuthenticated(HttpServletRequest request, HttpServletResponse response) throws Exception;
    }
    
    public void setUserNotAuthenticatedHandler(UserNotAuthenticatedHandler handler) {
        this.userNotAuthenticatedHandler = handler;
    }
    
    private class DefaultUserNotAuthenticatedHandler implements UserNotAuthenticatedHandler {
        @Override
        public boolean userNotAuthenticated(HttpServletRequest request, HttpServletResponse response) throws Exception {
            if (loginView == null) {
                throw new UserNotLoggedInException();
            }
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            View view = viewResolver.resolveViewName(loginView, RequestContextUtils.getLocale(request));
            view.render(Collections.<String, Object> emptyMap(), request, response);

            return false;
        }
    };
}
