package org.atlasapi.application.auth;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.users.NewUserSupplier;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.application.users.v3.User;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.social.model.TwitterUserDetails;

public class UserAuthCallbackHandler implements AuthCallbackHandler {

    private final UserStore userStore;
    private final NewUserSupplier newUser;
    private SubstitutionTableNumberCodec idCodec;

    public UserAuthCallbackHandler(UserStore store, NewUserSupplier userSupplier) {
        this.userStore = store;
        this.newUser = userSupplier;
        this.idCodec = new SubstitutionTableNumberCodec();
    }

    @Override
    public View handle(HttpServletResponse response, HttpServletRequest request, TwitterUserDetails userDetails, String redirectUri) {
        if (redirectUri != null) {
            return new RedirectView(redirectUri);
        } else {
            User user = userStore.userForRef(userDetails.getUserRef()).or(newUser);
            String userId = idCodec.encode(BigInteger.valueOf(user.getId()));
            String applicationsUri = String.format("/admin/users/%s/applications", userId);
            if (user.getUserRef() == null) {
                user.setUserRef(userDetails.getUserRef());
                userStore.store(user);
            }
            if (userDetailsIncomplete(user)) {
                userStore.store(populateFromUserDetails(user, userDetails));
                return new RedirectView(String.format("/admin/users/%s/account?redirectUri=%s", userId, applicationsUri));
            } else {
                return new RedirectView(applicationsUri);
            }
        }
    }
    
    private User populateFromUserDetails(User user, TwitterUserDetails userDetails) {
        if (isMissing(user.getScreenName())) {
            user.setScreenName(userDetails.getScreenName());
        }
        if (isMissing(user.getFullName())) {
            user.setFullName(userDetails.getFullName());
        }
        if (isMissing(user.getWebsite())) {
            user.setWebsite(userDetails.getHomepageUrl());
        }
        // These values are not available through Twitter
        if (isMissing(user.getEmail())) {
            user.setEmail("");
        }
        if (isMissing(user.getCompany())) {
            user.setCompany("");
        }
        return user;
    }
    
    private boolean userDetailsIncomplete(User user) {
        return isMissing(user.getScreenName()) || isMissing(user.getEmail()) || isMissing(user.getFullName());
    }
    
    private boolean isMissing(String value) {
        return value == null || value.isEmpty();
    }

}
