package org.atlasapi.application.auth;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.users.NewUserSupplier;
import org.atlasapi.application.users.User;
import org.atlasapi.application.users.UserStore;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.social.model.UserRef;

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
    public View handle(HttpServletResponse response, HttpServletRequest request, UserRef userRef, String redirectUri) {
        if (redirectUri != null) {
            return new RedirectView(redirectUri);
        } else {
            User user = userStore.userForRef(userRef).or(newUser);
            if (user.getUserRef() == null) {
                user.setUserRef(userRef);
                userStore.store(user);
            }
            return new RedirectView(String.format("/admin/users/%s/applications",idCodec.encode(BigInteger.valueOf(user.getId()))));
        }
    }

}
