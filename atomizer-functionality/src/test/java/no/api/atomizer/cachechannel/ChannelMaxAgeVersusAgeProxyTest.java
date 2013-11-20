package no.api.atomizer.cachechannel;

import no.api.atomizer.cachechannel.filter.HttpHeaderScrutinizer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <pre>
 *     ccaa : Last age is discarded
 *     caca : Normal
 *     caac : Normal
 *     acca : Normal
 *     acac : Normal
 *     aacc : First age is discarded, last maxage is assumed to have age=0
 * </pre>
 */
public class ChannelMaxAgeVersusAgeProxyTest {
    private MockHttpServletResponse mockRes;
    private ChannelMaxAgeVersusAgeProxy maxAgeVersusAge;

    @Before
    public void setUp() {
        mockRes = new MockHttpServletResponse();
        maxAgeVersusAge = ChannelMaxAgeVersusAgeProxy.createMaxAgeVersusAge(mockRes);
    }

    @Test
    public void simpleSetup() {
        assertEquals(0, mockRes.getHeaders(CacheControl.CACHE_CONTROL).size());
        assertEquals(0, mockRes.getHeaders(HttpHeaderScrutinizer.AGE).size());
        maxAgeVersusAge.addAge(100);
        maxAgeVersusAge.setMaxAge(150);
        assertEquals(1, mockRes.getHeaders(HttpHeaderScrutinizer.AGE).size());
        assertEquals("100", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
        assertTrue(maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=150"));
    }

    @Test
    public void age_shall_never_be_larger_than_maxage() {
        maxAgeVersusAge.setMaxAge(100);
        // Age should be discarded if it is found larger than maxage
        maxAgeVersusAge.addAge( 200 );

        assertTrue(""+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("0", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }

    @Test
    public void age_can_be_equal_to_maxage() {
        maxAgeVersusAge.setMaxAge( 100 );
        maxAgeVersusAge.addAge( 100 );

        assertTrue(""+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("100", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }

    /**
     *     ccaa : Last age is discarded
     *     first cachecontrol maxage used
     */
    @Test
    public void scenario_ccaa_1c() {
        maxAgeVersusAge.setMaxAge( 333 );
        // Implicit age=0
        maxAgeVersusAge.setMaxAge(340);
        maxAgeVersusAge.addAge( 6 );
        maxAgeVersusAge.addAge( 200 ); // discard

        assertTrue("Expected max-age 333, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=333"));
        assertEquals("0", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }

    /**
     *     ccaa : Last age is discarded
     *     second cachecontrol maxage used
     */
    @Test
    public void scenario_ccaa_2c() {
        maxAgeVersusAge.setMaxAge( 99 ); // 99 and age 0 : 99
        maxAgeVersusAge.setMaxAge( 100 ); // 100 and age 10 : 90 (to be kept)
        maxAgeVersusAge.addAge( 10 );
        maxAgeVersusAge.addAge( 11 ); // Discarded

        assertTrue("Expected max-age 100, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("10", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }

    /**
     *     caca : Normal
     *     First CC header wins
     */
    @Test
    public void scenario_caca_1() {
        maxAgeVersusAge.setMaxAge( 100 ); // 100 and age 30 : 70 (to be kept)
        maxAgeVersusAge.addAge( 30 );
        maxAgeVersusAge.setMaxAge( 90 ); // 90 and age 10 : 80
        maxAgeVersusAge.addAge( 10 );

        assertTrue("Expected max-age 100, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("30", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
    /**
     *     caca : Normal
     *     Second CC header wins
     */
    @Test
    public void scenario_caca_2() {
        maxAgeVersusAge.setMaxAge( 90 ); // 90 and age 10 : 80
        maxAgeVersusAge.addAge(10);
        maxAgeVersusAge.setMaxAge(100); // 100 and age 30 : 70 (to be kept)
        maxAgeVersusAge.addAge( 30 );

        assertTrue("Expected max-age 100, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("30", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }

    /**
     *     caac : Normal
     *     First header wins
     */
    @Test
    public void scenario_caac_1() {
        maxAgeVersusAge.setMaxAge( 100 ); // 100 and age 30 : 70 (to be kept)
        maxAgeVersusAge.addAge(30);
        maxAgeVersusAge.addAge(10);
        maxAgeVersusAge.setMaxAge(90); // 90 and age 10 : 80

        assertTrue("Expected max-age 100, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("30", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
    /**
     *     caac : Normal
     *     Second header wins
     */
    @Test
    public void scenario_caac_2() {
        maxAgeVersusAge.setMaxAge( 90 ); // 90 and age 10 : 80
        maxAgeVersusAge.addAge( 10 );
        maxAgeVersusAge.addAge(30);
        maxAgeVersusAge.setMaxAge(100); // 100 and age 30 : 70 (to be kept)

        assertTrue("Expected max-age 100, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("30", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
    /**
     *     acca : Normal
     *     first header wins
     */
    @Test
    public void scenario_acca_1() {
        maxAgeVersusAge.addAge( 30 );
        maxAgeVersusAge.setMaxAge(100); // 100 and age 30 : 70 (to be kept)
        maxAgeVersusAge.setMaxAge( 90 ); // 90 and age 10 : 80
        maxAgeVersusAge.addAge(10);

        assertTrue("Expected max-age 100, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("30", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
    /**
     *     acca : Normal
     *     second header wins
     */
    @Test
    public void scenario_acca_2() {
        maxAgeVersusAge.addAge( 10 );
        maxAgeVersusAge.setMaxAge( 90 ); // 90 and age 10 : 80
        maxAgeVersusAge.setMaxAge(100); // 100 and age 30 : 70 (to be kept)
        maxAgeVersusAge.addAge(30);

        assertTrue("Expected max-age 100, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("30", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
    /**
     *     acac : Normal
     *     first header wins
     */
    @Test
    public void scenario_acac_1() {
        maxAgeVersusAge.addAge(30);
        maxAgeVersusAge.setMaxAge(100); // 100 and age 30 : 70 (to be kept)
        maxAgeVersusAge.addAge( 10 );
        maxAgeVersusAge.setMaxAge( 90 ); // 90 and age 10 : 80

        assertTrue("Expected max-age 100, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("30", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
    /**
     *     acac : Normal
     */
    @Test
    public void scenario_acac_2() {
        maxAgeVersusAge.addAge( 10 );
        maxAgeVersusAge.setMaxAge( 90 ); // 90 and age 10 : 80
        maxAgeVersusAge.addAge(30);
        maxAgeVersusAge.setMaxAge(100); // 100 and age 30 : 70 (to be kept)

        assertTrue("Expected max-age 100, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=100"));
        assertEquals("30", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
    /**
     *     aacc : First age is discarded, last maxage is assumed to have age=0
     */
    @Test
    public void scenario_aacc_1() {
        maxAgeVersusAge.addAge( 10 );
        maxAgeVersusAge.addAge(30);
        maxAgeVersusAge.setMaxAge( 90 ); // 90 and age 30 : 60 (to be kept)
        maxAgeVersusAge.setMaxAge(70); // 70 and age 0 : 70

        assertTrue("Expected max-age 90, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=90"));
        assertEquals("30", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
    /**
     *     aacc : First age is discarded, last maxage is assumed to have age=0
     */
    @Test
    public void scenario_aacc_2() {
        maxAgeVersusAge.addAge( 10 );
        maxAgeVersusAge.addAge(30);
        maxAgeVersusAge.setMaxAge( 90 ); // 90 and age 30 : 60
        maxAgeVersusAge.setMaxAge(59); // 59 and age 0 : 59  (to be kept)

        assertTrue("Expected max-age 59, got: "+maxAgeVersusAge.toCacheChannelString(), maxAgeVersusAge.toCacheChannelString().contains("channel-maxage=59"));
        assertEquals("0", mockRes.getHeader(HttpHeaderScrutinizer.AGE));
    }
}
