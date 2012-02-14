package org.atlasapi.application.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import com.metabroadcast.common.social.model.UserRef;

public interface AuthCallbackHandler {

    View handle(HttpServletResponse response, HttpServletRequest request, UserRef user, String redirectUri);
    
}
