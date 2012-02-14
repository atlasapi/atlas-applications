package org.atlasapi.application.auth;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    public static final String ADMIN_LOGIN = "/admin/login";

    @RequestMapping(ADMIN_LOGIN)
    public String showLogin(HttpServletRequest request) {
        request.getSession(true);
        return "applications/login";
    }
    
}
