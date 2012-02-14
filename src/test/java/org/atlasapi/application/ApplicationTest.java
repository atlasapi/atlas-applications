package org.atlasapi.application;

import static org.junit.Assert.*;

import org.atlasapi.application.Application;
import org.junit.Test;


public class ApplicationTest {

    @Test
    public void shouldCreateApplication() {
        assertNotNull(Application.application("test-slug").withCredentials(new ApplicationCredentials("apiKey")).build());
    }
}
