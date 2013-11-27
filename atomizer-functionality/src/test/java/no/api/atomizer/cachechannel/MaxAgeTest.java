package no.api.atomizer.cachechannel;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class MaxAgeTest {
    @Test
    public void test_exchange_of_max_age_when_zero() {
        CacheControl cc = new CacheControl(CacheControl.DEFAULT_MAX_CACHE_CHANNEL_FIELD_LENGTH, new MockHttpServletResponse());
        cc.setMaxAge(0);
        Assert.assertTrue("The cache channel header should start with must-revalidate, but it does not: "+cc.toCacheChannelString(),
                cc.toCacheChannelString().startsWith("must-revalidate"));
    }

    @Test
    public void test_normal_operation() {
        CacheControl cc = new CacheControl(CacheControl.DEFAULT_MAX_CACHE_CHANNEL_FIELD_LENGTH, new MockHttpServletResponse());
        cc.setMaxAge(9000);
        Assert.assertTrue( "Expecting cache string to start with max-age=9000. Got: "+cc.toCacheChannelString(),
                cc.toCacheChannelString().startsWith("max-age=9000"));
        cc.setMaxAge(100);
        cc.setMaxAge(200);
        Assert.assertTrue("Expecting CC to start with max-age=100, but got: "+cc.toCacheChannelString(),
                cc.toCacheChannelString().startsWith("max-age=100"));
    }
}
