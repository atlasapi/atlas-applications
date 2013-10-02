package org.atlasapi.application.query;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.OldApplication;
import org.atlasapi.application.OldApplicationConfiguration;
import org.atlasapi.application.OldApplicationCredentials;
import org.atlasapi.application.OldApplicationStore;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.net.IpRange;

public class IpCheckingApiKeyConfigurationFetcher implements ApplicationConfigurationFetcher {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String API_KEY_QUERY_PARAMETER = "apiKey";
    
    private final OldApplicationStore reader;

    public IpCheckingApiKeyConfigurationFetcher(OldApplicationStore reader) {
        this.reader = reader;
    }
    
    @Override
    public ImmutableSet<String> getParameterNames() {
        return ImmutableSet.of(API_KEY_QUERY_PARAMETER);
    }

    @Override
    public Maybe<OldApplicationConfiguration> configurationFor(HttpServletRequest request) {
        if (request != null) {
            String apiKey = request.getParameter(API_KEY_QUERY_PARAMETER);
            if (apiKey != null) {
                Optional<OldApplication> app = reader.applicationForKey(apiKey);
                if (app.isPresent() && validIp(app.get().getCredentials(), request)) {
                    return Maybe.fromPossibleNullValue(app.get().getConfiguration());
                }
            }
        }
        return Maybe.nothing();
    }

    private boolean validIp(OldApplicationCredentials credentials, HttpServletRequest request) {
        if (credentials.getIpAddressRanges() == null || credentials.getIpAddressRanges().isEmpty()) {
            return true;
        }

        String remoteAddr = getAddressFrom(request);
        try {
            final InetAddress ipAddr = InetAddress.getByName(remoteAddr);
            Iterables.any(credentials.getIpAddressRanges(), new Predicate<IpRange>() {
                @Override
                public boolean apply(IpRange input) {
                    return input.isInRange(ipAddr);
                }
            });
            return false;
        } catch (UnknownHostException e) {
            return false;
        }

    }
    

    private String getAddressFrom(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (!Strings.isNullOrEmpty(forwardedFor)) {
            List<String> forwardList = ImmutableList.copyOf(Splitter.on(",").trimResults().omitEmptyStrings().split(forwardedFor));
            if (!forwardList.isEmpty()){
                return forwardList.get(forwardList.size()-1);
            }
        }
        return request.getRemoteAddr();
    }
}
