package org.atlasapi.application.v3;

import static org.junit.Assert.*;

import org.atlasapi.application.v3.Application;
import org.atlasapi.application.v3.ApplicationCredentials;
import org.junit.Test;


public class ApplicationTest {

    @Test
    public void shouldCreateApplication() {
        assertNotNull(Application.application("test-slug").withCredentials(new ApplicationCredentials("apiKey")).build());
    }
}
