package org.atlasapi.application;

import static org.junit.Assert.*;

import org.atlasapi.application.OldApplication;
import org.junit.Test;


public class ApplicationTest {

    @Test
    public void shouldCreateApplication() {
        assertNotNull(OldApplication.application("test-slug").withCredentials(new ApplicationCredentials("apiKey")).build());
    }
}
