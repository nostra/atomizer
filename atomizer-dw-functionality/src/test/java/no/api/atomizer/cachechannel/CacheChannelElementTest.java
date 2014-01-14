package no.api.atomizer.cachechannel;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CacheChannelElementTest {

    @Test
    public void testIsLegalKey() throws Exception {

        CacheChannelElement illegalElement = new CacheChannelElement( "foo", "bar");
        CacheChannelElement legalElement = new CacheChannelElement( "max-age","1400");
        CacheChannelElement legalElement2 = new CacheChannelElement( "channel-maxage","1400");

        Assert.assertFalse( "Detection of illegal key", illegalElement.isLegalKey());
        Assert.assertTrue( "Verification of legal key", legalElement.isLegalKey());
        Assert.assertTrue( "Verification of legal key", legalElement2.isLegalKey());

    }

    @Test
    public void testIsLegalBasedOnKey() throws Exception {

        CacheChannelElement illegalElement = new CacheChannelElement( "foo", "bar");
        CacheChannelElement legalElement = new CacheChannelElement( "max-age","1400");

        Assert.assertFalse( "Detection of illegal element", illegalElement.isLegal());
        Assert.assertTrue( "Verification of legal element", legalElement.isLegal());

    }

    @Test
    public void testFilteredElements() {

        CacheChannelElement preCheckElement = new CacheChannelElement( "pre-check", "600" );
        CacheChannelElement postCheckElement = new CacheChannelElement( "post-check", "600" );
        CacheChannelElement mustRevalidateElement = new CacheChannelElement( "must-revalidate", null );
        CacheChannelElement noCacheElement = new CacheChannelElement( "no-cache", null );

        Assert.assertFalse( "Check filtering of pre-check", preCheckElement.isLegal() );
        Assert.assertFalse( "Check filtering of post-check", postCheckElement.isLegal() );
        Assert.assertFalse( "Check filtering of must-revalidate", mustRevalidateElement.isLegal() );
        Assert.assertFalse( "Check filtering of no-cache", noCacheElement.isLegal() );

    }
}
