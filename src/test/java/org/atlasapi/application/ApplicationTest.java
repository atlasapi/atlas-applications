package org.atlasapi.application;

import static org.junit.Assert.*;

import org.junit.Test;


public class ApplicationTest {

    @Test
    public void shouldCreateApplication() {
        assertNotNull(new Application("test-slug"));
    }
}
