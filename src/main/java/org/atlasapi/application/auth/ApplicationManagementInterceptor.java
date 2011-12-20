package org.atlasapi.application.auth;

import static org.atlasapi.application.users.Role.ADMIN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.users.User;
import org.atlasapi.application.users.UserStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.social.auth.RequestScopedAuthenticationProvider;

public class ApplicationManagementInterceptor extends HandlerInterceptorAdapter {

    private Pattern applicationPagePattern = Pattern.compile("/admin/applications/([a-z0-9][a-z0-9\\-]{1,255})/?.*");
    private RequestScopedAuthenticationProvider authService;
    private final UserStore store;
    
    public ApplicationManagementInterceptor(UserStore store) {
        this.store = store;
    }
        
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        final User user = user();
        if (isApplicationPage(request) && (!user.is(ADMIN) || !user.manages(applicationSlug(request)))) {
            response.setStatus(HttpStatusCode.FORBIDDEN.code());
            response.setContentLength(0);
            return false;
        }
        
        return true;
    }

    private String applicationSlug(HttpServletRequest request) {
        final Matcher matcher = applicationPagePattern.matcher(request.getRequestURI());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalStateException();
    }

    private boolean isApplicationPage(HttpServletRequest request) {
        return applicationPagePattern.matcher(request.getRequestURI()).matches();
    }

    public User user() {
        return store.userForRef(authService.principal()).get();
    }

    @Autowired
    public void setAuthService(RequestScopedAuthenticationProvider authService) {
        this.authService = authService;
    }
    
}
