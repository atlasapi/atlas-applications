package org.atlasapi.application.query;

import com.metabroadcast.applications.client.exceptions.ErrorCode;

public class ApplicationNotFoundException extends ApplicationFetchException {

    private String request;

    private ApplicationNotFoundException(String key, ErrorCode errorCode, String request) {
        super(key, errorCode);
        this.request = request;
    }

    public static ApplicationNotFoundException create(String request) {
        return new ApplicationNotFoundException("", ErrorCode.NOT_FOUND, request);
    }

    @Override
    public String getMessage() {
        return String.format(
                "Unable to resolve an application for request: %s",
                request
        );
    }

}
