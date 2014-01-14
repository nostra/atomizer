package no.api.atomizer.cachechannel.client;

import no.api.atomizer.cachechannel.CacheControl;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Date;
import java.util.GregorianCalendar;

public class CacheChannelHeaderTest {

    @Test
    public void testAddCacheChannelHeader() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        CacheChannelHeader cch = new CacheChannelHeader(response);
        cch.addCacheChannelHeader("/art999");
        Assert.assertNotNull(response.getHeader(CacheControl.CACHE_CONTROL));
        Assert.assertTrue(((String) response.getHeader(CacheControl.CACHE_CONTROL)).contains("art999"));
    }

    @Test
    public void testAdjustMaxAgeTo() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        CacheChannelHeader cch = new CacheChannelHeader(response);
        cch.adjustChannelMaxAgeTo(998877L);
        Assert.assertNotNull(response.getHeader(CacheControl.CACHE_CONTROL));
        Assert.assertTrue(((String) response.getHeader(CacheControl.CACHE_CONTROL)).contains("channel-maxage=998877"));
    }

    @Test
    public void testAddCacheChannelHeaderWith300InMaxAge() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        CacheChannelHeader cch = new CacheChannelHeader(response);
        cch.addCacheChannelHeaderWith300InChannelMaxAge("/art999");
        Assert.assertNotNull(response.getHeader(CacheControl.CACHE_CONTROL));
        Assert.assertTrue(((String) response.getHeader(CacheControl.CACHE_CONTROL)).contains("channel-maxage=300"));
    }


    @Test
    public void testMidnightCC() {
        CacheChannelHeader cch = new CacheChannelHeader(new MockHttpServletResponse());
        //22:00 = testdate. 24:00 - 22:00 = 2h = 120min = 7200 sec
        Date testDate = new GregorianCalendar(2009, 01, 01, 22, 00).getTime();
        long result = cch.calculateMidnightPrefix(testDate);
        Assert.assertEquals(7200, result);

        //month shift
        //22:00 = testdate. 24:00 - 22:00 = 2h = 120min = 7200 sec
        testDate = new GregorianCalendar(2009, 06, 31, 22, 00).getTime();
        result = cch.calculateMidnightPrefix(testDate);
        Assert.assertEquals(7200, result);

        //year shift
        //22:00 = testdate. 24:00 - 22:00 = 2h = 120min = 7200 sec
        testDate = new GregorianCalendar(2009, 11, 31, 22, 00).getTime();
        result = cch.calculateMidnightPrefix(testDate);
        Assert.assertEquals(7200, result);
    }


    @Test
    public void testMinutesCC() {
        CacheChannelHeader cch = new CacheChannelHeader(new MockHttpServletResponse());
        //22:00 = testdate. expected: 22:05 = 300 sec
        Date testDate = new GregorianCalendar(2009, 01, 01, 22, 00, 00).getTime();
        long result = cch.calculateMinuteMaxagePrefix(testDate, 5);
        Assert.assertEquals(300, result);

        //21:59:59 = testdate. expected: 22:00 = 1 sec
        testDate = new GregorianCalendar(2009, 01, 01, 21, 59, 59).getTime();
        result = cch.calculateMinuteMaxagePrefix(testDate, 5);
        Assert.assertEquals(1, result);

        //22:01 = testdate. expected: 22:05 = 240 sec
        testDate = new GregorianCalendar(2009, 01, 01, 22, 01, 00).getTime();
        result = cch.calculateMinuteMaxagePrefix(testDate, 5);
        Assert.assertEquals(240, result);

        //22:02:30 = testdate. expected: 22:05 = 150 sec
        testDate = new GregorianCalendar(2009, 01, 01, 22, 02, 30).getTime();
        result = cch.calculateMinuteMaxagePrefix(testDate, 5);
        Assert.assertEquals(150, result);

        //21:59:59 = testdate. expected: 22:00 = 1 sec
        testDate = new GregorianCalendar(2009, 01, 01, 21, 59, 59).getTime();
        result = cch.calculateMinuteMaxagePrefix(testDate, 5);
        Assert.assertEquals(1, result);

        //23:59:59 = testdate. expected: 00:00 = 1 sec
        testDate = new GregorianCalendar(2009, 01, 01, 23, 59, 59).getTime();
        result = cch.calculateMinuteMaxagePrefix(testDate, 5);
        Assert.assertEquals(1, result);
    }

}
