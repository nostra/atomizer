package no.api.atomizer.cachechannel;

import no.api.atomizer.cachechannel.filter.HttpHeaderScrutinizer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The director knows when data have finished transferring. This can ensure that
 * spurious headers are treated more correctly. So it is an edge case insurance.
 */
public class MaxAgeBumpTest {
    private MockHttpServletResponse mockRes;
    private ChannelMaxAgeVersusAgeProxy maxAgeVersusAge;

    @Before
    public void setUp() {
        mockRes = new MockHttpServletResponse();
        maxAgeVersusAge = ChannelMaxAgeVersusAgeProxy.createMaxAgeVersusAge(mockRes);
    }

    @Test
    public void cornercase_aBca() {
        maxAgeVersusAge.addAge( 10 ); // To be discarded as it is a lone age
        maxAgeVersusAge.streamStart();
        maxAgeVersusAge.setMaxAge( 90 );
        maxAgeVersusAge.addAge(5);
        maxAgeVersusAge.streamEnd();
        maxAgeVersusAge.addAge(1); // Discarded as lone age

        assertTrue("Expected channel-maxage 90, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=90"));
        assertEquals("5", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }

    @Test
    public void cornercase_acBa() {
        maxAgeVersusAge.streamStart();
        maxAgeVersusAge.addAge( 10 );
        maxAgeVersusAge.setMaxAge( 90 );
        maxAgeVersusAge.streamEnd();
        maxAgeVersusAge.streamStart();
        maxAgeVersusAge.addAge(1); // Discarded as lone age
        maxAgeVersusAge.streamEnd();

        assertTrue("Expected channel-maxage 90, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=90"));
        assertEquals("10", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }

    @Test
    public void cornerCase_aBacBacBaaB() {
        maxAgeVersusAge.addAge( 100 );
        maxAgeVersusAge.streamStart();
        maxAgeVersusAge.addAge( 90 ); // To be kept, 150-90:60
        maxAgeVersusAge.setMaxAge( 150 );
        maxAgeVersusAge.streamEnd();
        maxAgeVersusAge.streamStart();
        maxAgeVersusAge.addAge( 10 );
        maxAgeVersusAge.setMaxAge( 120 );  // To be discarded: 120-10: 110
        maxAgeVersusAge.streamEnd();
        maxAgeVersusAge.streamStart();
        maxAgeVersusAge.addAge( 110 ); // Shall be discarded
        maxAgeVersusAge.addAge( 130 ); // Shall be discarded
        maxAgeVersusAge.streamEnd();

        assertTrue("Expected channel-maxage 150, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=150"));
        assertEquals("90", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
}