package no.api.atomizer.cachechannel.transform;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class Date2StringTest {
    @Test
    public void testTransformDateToString() {
        Date now = new Date();
        String dtext = Date2String.transformDateToRfc1123String(now);
        long ldt = String2Date.transformStringDate(dtext, -1);
        // Division and multiplication is to get rid of milliseconds.
        assertEquals((now.getTime()/1000)*1000,  ldt );
        assertEquals(dtext, Date2String.transformDateToRfc1123String(new Date(ldt)) );
        assertTrue("Date shall be in GMT, but was "+dtext, dtext.endsWith("GMT"));
    }

}
