package org.atlasapi.application.v3;

import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mongodb.DBObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ApplicationConfigurationTranslatorTest {

    private ApplicationConfigurationTranslator translator;

    @Before
    public void setUp() throws Exception {
        translator = new ApplicationConfigurationTranslator();
    }

    @Test
    public void testEncodesAndDecodesApplicationConfiguration() {
        
        ApplicationConfiguration config = ApplicationConfiguration.defaultConfiguration()
                .agreeLicense(Publisher.BBC)
                .enable(Publisher.BBC)
                .request(Publisher.ITV)
                .approve(Publisher.ITV)
                .enable(Publisher.ITV)
                .request(Publisher.NETFLIX)
                .copyWithPrecedence(ImmutableList.of(Publisher.ITV, Publisher.BBC))
                .copyWithWritableSources(ImmutableSet.of(Publisher.ITV))
                .copyWithContentHierarchyPrecedence(ImmutableList.of(Publisher.BBC, Publisher.ITV));
        
        DBObject dbo = translator.toDBObject(config);
        
        ApplicationConfiguration decoded = translator.fromDBObject(dbo);
        
        assertTrue(decoded.isEnabled(Publisher.ITV));
        assertTrue(decoded.isEnabled(Publisher.BBC));
        assertThat(decoded.orderdPublishers().get(0), is(Publisher.ITV));
        assertThat(decoded.orderdPublishers().get(1), is(Publisher.BBC));
        assertTrue(decoded.canWrite(Publisher.ITV));
        assertEquals(SourceStatus.REQUESTED, decoded.statusOf(Publisher.NETFLIX));
        assertEquals(SourceStatus.AVAILABLE_DISABLED, decoded.statusOf(Publisher.PA));
        assertEquals(config.contentHierarchyPrecedence().get(), decoded.contentHierarchyPrecedence().get());
    }

    @Test
    public void serialisesAndDeserialisesAccessRoles() throws Exception {
        ApplicationAccessRole expectedRole = ApplicationAccessRole.OWL_ACCESS;

        ApplicationConfiguration configuration = getBuilder()
                .withAccessRoles(ImmutableSet.of(expectedRole))
                .build();

        ApplicationConfiguration actual = translator.fromDBObject(
                translator.toDBObject(configuration)
        );

        assertThat(
                actual.getAccessRoles().contains(expectedRole),
                is(true)
        );
    }

    @Test
    public void serialisesDefaultRolesEvenWhenNotExplicitlyProvided() throws Exception {
        ApplicationConfiguration configuration = getBuilder()
                .build();

        ApplicationConfiguration actual = translator.fromDBObject(
                translator.toDBObject(configuration)
        );

        assertThat(
                actual.getAccessRoles().containsAll(ApplicationAccessRole.getDefaultRoles()),
                is(true)
        );
    }

    private ApplicationConfiguration.Builder getBuilder() {
        return ApplicationConfiguration.builder()
                .withSourceStatuses(ImmutableMap.of(
                        Publisher.METABROADCAST,
                        SourceStatus.AVAILABLE_ENABLED
                ))
                .withPrecedence(ImmutableList.of(Publisher.METABROADCAST))
                .withWritableSources(ImmutableSet.of())
                .withImagePrecedenceEnabled(false)
                .withContentHierarchyPrecedence(Optional.absent());
    }
}
