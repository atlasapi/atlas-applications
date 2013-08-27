package org.atlasapi.application.auth;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.auth.CookieTranslator;
import com.metabroadcast.common.social.auth.credentials.AuthToken;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.social.twitter.TwitterApplication;
import com.metabroadcast.common.social.user.AccessTokenProcessor;
import com.metabroadcast.common.url.UrlEncoding;

@Controller
public class TwitterAuthController {

    public static final String LOGOUT = "/admin/logout";
    public static final String LOGIN_URL = "/admin/login/twitter";
    public static final String LOGIN_FAILED_URL = "/admin/login/failed";
    public static final String CALLBACK_URL = "/admin/login/oauth-callback";

    private final CookieTranslator cookieTranslator;
    private final String host;
    private final AccessTokenProcessor accessTokenProcessor;
    private final TwitterFactory twitterFactory;
    private final AuthCallbackHandler callbackHandler;

    public TwitterAuthController(TwitterApplication twitterApplication, AccessTokenProcessor accessTokenProcessor, CookieTranslator cookieTranslator, AuthCallbackHandler callbackHandler, String host) {
        this.accessTokenProcessor = accessTokenProcessor;
        this.cookieTranslator = cookieTranslator;
        this.callbackHandler = callbackHandler;
        this.host = host;
        this.twitterFactory = new TwitterFactory(
            new ConfigurationBuilder()
                .setOAuthConsumerKey(twitterApplication.getConsumerKey())
                .setOAuthConsumerSecret(twitterApplication.getConsumerSecret())
            .build()
        );
    }

    @RequestMapping(value = { LOGIN_URL }, method = RequestMethod.GET)
    public View login(HttpServletRequest request, @RequestParam(required = false) String targetUri) {
        try {
            Twitter twitter = twitterFactory.getInstance();
            RequestToken requestToken = twitter.getOAuthRequestToken(getCallbackUrl(Maybe.fromPossibleNullValue(targetUri)));
            request.getSession().setAttribute("requestToken", requestToken);
            request.getSession().setAttribute("twitter", twitter);
            return new RedirectView(requestToken.getAuthenticationURL());
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String getCallbackUrl(Maybe<String> targetUri) {
        String callbackUrl = "http://" + host + CALLBACK_URL;
        
        if (targetUri.hasValue()) {
            callbackUrl += "?targetUri=" + UrlEncoding.encode(targetUri.requireValue());
        }
        
        return callbackUrl;
    }

    @RequestMapping(value = CALLBACK_URL)
    public View oauthCallback(Map<String, Object> model, HttpServletResponse response, HttpServletRequest request, @RequestParam(required = false) String targetUri) {
        Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        String verifier = request.getParameter("oauth_verifier");

        try {
            AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);
            request.getSession().removeAttribute("requestToken");

            Maybe<UserRef> user = accessTokenProcessor.process(new AuthToken(token.getToken(), token.getTokenSecret(), UserNamespace.TWITTER, null));
            twitter.setOAuthAccessToken(token);
            if (user.hasValue()) {
                cookieTranslator.toResponse(response, user.requireValue());
                UserDetails userDetails = getUserDetails(twitter, user.requireValue());
                return callbackHandler.handle(response, request, userDetails, targetUri);
            } else {
                return new RedirectView(LOGIN_FAILED_URL);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private UserDetails getUserDetails(Twitter twitter, UserRef user) throws TwitterException {
        UserDetails userDetails = new UserDetails(user);
        long userIds[] = new long[1];
        userIds[0] = Long.valueOf(user.getUserId());
        ResponseList<User> lookupResult = twitter.lookupUsers(userIds);
        if (!lookupResult.isEmpty()) {
            User twUser = lookupResult.get(0);
            userDetails = userDetails
                          .withScreenName(twUser.getScreenName())
                          .withFullName(twUser.getName())
                          .withProfileUrl(twUser.getURL());
        }
        return userDetails;
    }

    @RequestMapping(LOGIN_FAILED_URL)
    public String loginFailed(Map<String, Object> model) {
        return "login/failed";
    }
    
    @RequestMapping(LOGOUT)
    public View logout(HttpServletResponse response) {
        
        cookieTranslator.removeCookie(response);
        
        return new RedirectView("/admin");
    }
}
