package no.api.atomizer.header.filter;

import no.api.atomizer.header.filter.internal.HeaderCollector;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class HeaderCollectorTest {

    private MockHttpServletResponse response;

    @Before
    public void setUp() {
        response = new MockHttpServletResponse();
    }

    @Test
    public void testAddHeaderFunctionality() throws IOException {
        HeaderCollector hc = new HeaderCollector(response);
        hc.addHeader("string", "a");
        hc.addHeader("string", "b");
        hc.addHeader("string", "c");
        hc.getOutputStream().println("whee");
        assertEquals(1, hc.getHeaderNames().size());
        assertEquals(3, hc.getHeaders("string").size());

    }

        @Test
    public void testContainsHeader() throws IOException {
        HeaderCollector hc = new HeaderCollector(response);
        hc.addDateHeader("date", new Date().getTime());
        assertTrue(hc.containsHeader("date"));
        assertFalse(hc.containsHeader("non-existing"));
        hc.addIntHeader("integer", 12345);
        hc.addHeader("string", "a");
        assertEquals(3, hc.getHeaderNames().size());
        hc.addHeader("string", "b");
        assertEquals(3, hc.getHeaderNames().size());
        assertEquals(2, hc.getHeaders("string").size());
        hc.setHeader("string", "c");
        Collection<String> h = hc.getHeaders("string");
        assertEquals("When using set, the last element with a given value is overwritten", 2, h.size());
        assertEquals(2, h.size());
        for ( String s : h ) {
            assertNotEquals("Element b should be replaced with c", "b", s);
        }
        assertEquals("No headers should be set yet", 0, response.getHeaderNames().size());
        hc.getWriter().println("whee");
        assertEquals("whee\n", response.getContentAsString());
        assertEquals("Headers should be flushed when output has been given.\nExpected :"+hc.getHeaderNames()+
                "\nGot: "+response.getHeaderNames()+"\n",
                hc.getHeaderNames().size(), response.getHeaderNames().size());
    }
}
