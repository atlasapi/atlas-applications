package org.atlasapi.application.v3;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Test;
import com.metabroadcast.common.time.DateTimeZones;


public class ApplicationTranslatorTest {

    @Test
    public void testTranslation() {
        DateTime fixed = new DateTime(DateTimeZones.UTC).withDate(2013, 12, 13).withTime(9, 10, 20, 0);
        Application app = Application.application("test123")
                .withTitle("test")
                .withDescription("desc")
                .withConfiguration(ApplicationConfiguration.DEFAULT_CONFIGURATION)
                .withCredentials(new ApplicationCredentials("apiKey"))
                .withLastUpdated(fixed)
                .withDeerId(123L)
                .withRevoked(true)
                .withNumberOfUsers(27L)
                .withStripeCustomerId("Stripe1234")
                .build();
        
        ApplicationTranslator translator = new ApplicationTranslator();
        Application translated = translator.fromDBObject(translator.toDBObject(app));
        assertEquals(app.getSlug(), translated.getSlug());
        assertEquals(app.getDescription(), translated.getDescription());
        assertEquals(app.getLastUpdated(), translated.getLastUpdated());
        assertEquals(app.getDeerId(), translated.getDeerId());
        assertEquals(app.isRevoked(), translated.isRevoked());
        assertEquals(app.getNumberOfUsers(), translated.getNumberOfUsers());
        assertEquals(app.getStripeCustomerId().get(), translated.getStripeCustomerId().get());
    }
}
