package no.api.atomizer.header.structure;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class CacheChannelStructureTest {

    @Test
    public void testAddCacheControlHeader() {
        CacheChannelStructure cc = new CacheChannelStructure();
        String original = "max-age=30, channel=\"http://event.api.no/atomizer/event/current\", " +
                "channel-maxage=86400, group=\"/pub41\", group=\"/sec71\", group=\"/art6596253\"";
        cc.addCacheControlHeader(original);
        assertTrue("Expecting to have contents after adding header", cc.hasContents());
        String contents = cc.toCacheChannelString();
        assertEquals("Not expecting any transformation of legal string", original, contents);
    }

    @Test
    public void testThatDuplicateGroupsAreOk() {
        CacheChannelStructure cc = new CacheChannelStructure();
        String original = "max-age=30, channel-maxage=86400";
        cc.addCacheControlHeader(original+", group=\"/pub41\"");
        cc.addCacheControlHeader(original+", group=\"/pub41\"");
        assertEquals("In the output string the pub41 group is repeated only once", original + ", group=\"/pub41\"",
                cc.toCacheChannelString());
    }

    @Test
    public void testMaxAgeSetting() {
        CacheChannelStructure cc = new CacheChannelStructure();
        cc.addCacheControlHeader("max-age=30, channel-maxage=86400");
        assertEquals("max-age=30, channel-maxage=86400", cc.toCacheChannelString());
        cc.addCacheControlHeader("max-age=10");
        cc.addCacheControlHeader("max-age=15");
        assertEquals("max-age=10, channel-maxage=86400", cc.toCacheChannelString());
    }

    @Test
    public void testChannelMaxAge() {
        CacheChannelStructure cc = new CacheChannelStructure();
        cc.addCacheControlHeader("channel-maxage=1000");
        assertEquals("max-age=30, channel-maxage=1000", cc.toCacheChannelString());
        cc.addCacheControlHeader("max-age=0, channel-maxage=800");
        assertEquals("max-age=0, channel-maxage=800", cc.toCacheChannelString());
        cc.addCacheControlHeader("max-age=0, channel-maxage=900");
        assertEquals("max-age=0, channel-maxage=800", cc.toCacheChannelString());
    }

    @Test
    public void testDefaultValues() {
        CacheChannelStructure cc = new CacheChannelStructure();
        assertEquals("max-age=30, channel-maxage=86400", cc.toCacheChannelString());
        cc.addCacheControlHeader("group=\"junit\"");
        assertEquals("max-age=30, channel-maxage=86400, group=\"junit\"", cc.toCacheChannelString());
    }

    @Test
    public void testNegativeAndWrongValues() {
        CacheChannelStructure cc = new CacheChannelStructure();
        cc.addCacheControlHeader("max-age=20, channel-maxage=900");
        String original = cc.toCacheChannelString();
        cc.addCacheControlHeader("channel-maxage=-1000");
        cc.addCacheControlHeader("channel-maxage=-1");
        cc.addCacheControlHeader("channel-maxage=ABC");
        cc.addCacheControlHeader("channel-maxage");
        cc.addCacheControlHeader("max-age=-1000");
        cc.addCacheControlHeader("max-age=-1");
        cc.addCacheControlHeader("max-age=ABC");
        cc.addCacheControlHeader("max-age");
        assertEquals("When getting erroneous adjustments, the string still is the same.", original, cc.toCacheChannelString());
    }

    @Test
    public void testWhenHavingTooLongGroups() {
        String groups="group=\"/a\", group=\"/b\", group=\"/c\"";
        CacheChannelStructure cc = new CacheChannelStructure(groups.length());
        StringBuilder sb = cc.trimChannelStringAndAddTooLongGroup(new StringBuilder(groups));
        assertEquals("group=\"/a\", "+CacheChannelStructure.TOO_LONG_GROUP, sb.toString());
    }

    @Test
    public void when_having_max_age_but_not_channel_max_age() {
        CacheChannelStructure cc = new CacheChannelStructure();
        cc.addCacheControlHeader("max-age=30");
        assertEquals("When no channel-maxage is supplied at all, set it equal to max-age", "max-age=30, channel-maxage=30", cc.toCacheChannelString());
        cc.addCacheControlHeader("channel-maxage=1234");
        assertEquals("max-age=30, channel-maxage=1234", cc.toCacheChannelString());
    }

    @Test
    public void channel_max_age_should_not_be_less_than_max_age() {
        CacheChannelStructure cc = new CacheChannelStructure();
        cc.addCacheControlHeader("max-age=30");
        cc.addCacheControlHeader("channel-maxage=20");
        assertEquals("max-age=20, channel-maxage=20", cc.toCacheChannelString());
    }

    /**
     * TODO Need to think through how to add this to code
     */
    @Test
    @Ignore
    public void testOtherCacheHeaders() {
        String[] headers = new String[]{
            "public",
            "private",
            "no-cache",
            "no-store",
            "no-transform",
            "must-revalidate",
            "proxy-revalidate",
            "s-maxage",
            "cache-extension"
        };
        for ( String header : headers ) {
            CacheChannelStructure cc = new CacheChannelStructure();
            cc.addCacheControlHeader(header);
            assertEquals(""+headers, cc.toCacheChannelString());
        }

    }
}
