package no.api.atomizer.header.structure;

import no.api.atomizer.cachechannel.CacheControl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Moving old tests, to be used for testing previous functionality
 */
public class CacheControlRegressTest {
    private static final String testCacheControl =
            "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                    "group=\"/pub123\", group=\"/pub456\", group=\"/sec0\", group=\"/shadow\", " +
                    "group=\"/sec111\", group=\"/art4800743\", group=\"/art4501671\", group=\"/art4796297\", " +
                    "group=\"/art4671392\", group=\"/art4800739\", group=\"/art4179244\", group=\"/art4179379\", " +
                    "group=\"/art4281399\", group=\"/art4281452\", group=\"/art3735964\", group=\"/art4799532\", " +
                    "group=\"/art4799604\", group=\"/art4800680\", group=\"/art4587827\", group=\"/art4587818\", " +
                    "group=\"/art4585716\", group=\"/art4585445\", group=\"/art4797748\", group=\"/art4799661\", " +
                    "group=\"/art4800377\", group=\"/art2660671\", group=\"/art4656344\", group=\"/art4277233\", " +
                    "group=\"/art4277231\", group=\"/art4199769\", group=\"/art2669540\", group=\"/art2698601\", " +
                    "group=\"/art4179382\", group=\"/art3662506\", group=\"/art4800163\", group=\"/art4800162\", " +
                    "group=\"/art4800160\", group=\"/art4800115\", group=\"/art4800580\", group=\"/art4800573\", " +
                    "group=\"/art4800564\", group=\"/art4800554\", group=\"/art4800547\", group=\"/art4797069\", " +
                    "group=\"/art4798537\", group=\"/art4798681\", group=\"/art4798454\", group=\"/art4798728\", " +
                    "group=\"/art4798811\", group=\"/art4798928\", group=\"/art4799652\", group=\"/art4798702\", " +
                    "group=\"/art3735912\", group=\"/art3553923\", group=\"/art4800922\", group=\"/art4800920\", " +
                    "group=\"/art4800919\", group=\"/art4799653\", group=\"/art4799891\", group=\"/art4800448\", " +
                    "group=\"/art4800349\", group=\"/art4799528\", group=\"/art4800925\", group=\"/art4800392\", " +
                    "group=\"/art4797977\", group=\"/art4797990\", group=\"/art4800918\", group=\"/art4800165\", " +
                    "group=\"/art4800164\", group=\"/art4800334\", group=\"/art4799127\", group=\"/art4799922\", " +
                    "group=\"/art4799805\", group=\"/art4800381\", group=\"/art4800113\", group=\"/art4800111\", " +
                    "group=\"/art4798214\", group=\"/art2646180\", group=\"/art4800378\", group=\"/art4800380\", " +
                    "group=\"/art4800379\", group=\"/art2646180\"";

    private static final String testCacheControl2 =
            "max-age=300, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=300, " +
                    "group=\"/pub123\", group=\"/pub456\", group=\"/shadow\", group=\"/sec0\", group=\"/sec111\", " +
                    "group=\"/art2646180\", group=\"/art2660671\", group=\"/art2669540\", group=\"/art2698601\", " +
                    "group=\"/art3553923\", group=\"/art3662506\", group=\"/art3735912\", group=\"/art3735964\", " +
                    "group=\"/art4179244\", group=\"/art4179379\", group=\"/art4179382\", group=\"/art4199769\", " +
                    "group=\"/art4277231\", group=\"/art4277233\", group=\"/art4281399\", group=\"/art4281452\", " +
                    "group=\"/art4501671\", group=\"/art4585445\", group=\"/art4585716\", group=\"/art4587818\", " +
                    "group=\"/art4587827\", group=\"/art4656344\", group=\"/art4671392\", group=\"/art4796297\", " +
                    "group=\"/art4797069\", group=\"/art4797748\", group=\"/art4797977\", group=\"/art4797990\", " +
                    "group=\"/art4798214\", group=\"/art4798454\", group=\"/art4798537\", group=\"/art4798681\", " +
                    "group=\"/art4798702\", group=\"/art4798728\", group=\"/art4798811\", group=\"/art4798928\", " +
                    "group=\"/art4799127\", group=\"/art4799528\", group=\"/art4799532\", group=\"/art4799604\", " +
                    "group=\"/art4799652\", group=\"/art4799653\", group=\"/art4799661\", group=\"/art4799805\", " +
                    "group=\"/art4799891\", group=\"/art4799922\", group=\"/art4800111\", group=\"/art4800113\", " +
                    "group=\"/art4800115\", group=\"/art4800160\", group=\"/art4800162\", group=\"/art4800163\", " +
                    "group=\"/art4800164\", group=\"/art4800165\", group=\"/art4800334\", group=\"/art4800349\", " +
                    "group=\"/art4800377\", group=\"/art4800378\", group=\"/art4800379\", group=\"/art4800380\", " +
                    "group=\"/art4800381\", group=\"/art4800392\", group=\"/art4800448\", group=\"/art4800547\", " +
                    "group=\"/art4800554\", group=\"/art4800564\", group=\"/art4800573\", group=\"/art4800580\", " +
                    "group=\"/art4800680\", group=\"/art4800739\", group=\"/art4800743\", group=\"/art4800918\", " +
                    "group=\"/art4800919\", group=\"/art4800920\", group=\"/art4800922\", group=\"/art4800925\"";


    @Test
    public void testaddCacheControlHeader() {
        CacheChannelStructure cc = new CacheChannelStructure();
        cc.addCacheControlHeader(testCacheControl);
        assertEquals(1400, cc.getMaxAge());
        assertEquals(1400, cc.getChannelMaxAge());
    }

    /**
     * TODO Follow up: Are we going to have default values?
     */
    @Ignore
    @Test
    public void testDefaultMaxAge() {
        CacheChannelStructure cc = new CacheChannelStructure();
        assertEquals(CacheControl.DEFAULT_CHANNEL_MAX_AGE, cc.getChannelMaxAge());
        assertEquals(CacheControl.DEFAULT_MAX_AGE, cc.getMaxAge());
    }

    @Test
    public void testResettingMaxAge() {
        // TODO Is this to be supported?
    }

    @Test
    public void testResettingMaxAgeExpr() {
        CacheChannelStructure cc = new CacheChannelStructure();
        cc.addCacheControlHeader("max-age=3210");

        Assert.assertTrue(cc.toCacheChannelString().contains("max-age=3210"));
    }

    @Test
    public void testSetMaxAge() {
        // TODO Is this to be supported?
    }


    @Test
    public void testThatTheCorrectArticleIsReplacedWhenMaxFieldLengthExceeded() {
        String testString = "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                "group=\"/art2\", group=\"/art1\", group=\"/art3\", group=\"/art4\"";
        CacheChannelStructure cc = new CacheChannelStructure(testString.length());
        cc.addCacheControlHeader(testString);
        cc.addCacheControlHeader("group=\"/art5\"");
        String cacheChannelString = cc.toCacheChannelString();
        assertTrue( "Expecting the channel string not to exceed the teststring in length. But it did: cc string: "+
                cacheChannelString.length()+", whereas the length of the original is "+testString.length(),
                cacheChannelString.length() < testString.length());
        assertEquals(
                    "max-age=300, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=300, " +
                            "group=\"/art2\", group=\"/art1\", "+CacheChannelStructure.TOO_LONG_GROUP,
                    cacheChannelString );
    }

    @Test
    public void testThatTheCorrectSectionIsReplacedWhenMaxFieldLengthExceeded() {
        String testString = "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                "group=\"/sec2\", group=\"/sec1\", group=\"/sec3\", group=\"/sec4\"";
        CacheChannelStructure cc = new CacheChannelStructure(testString.length());
        cc.addCacheControlHeader(testString);
        cc.addCacheControlHeader("group=\"/sec5\"");
        assertEquals("max-age=300, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=300, " +
                            "group=\"/sec2\", group=\"/sec1\", "+CacheChannelStructure.TOO_LONG_GROUP,
                    cc.toCacheChannelString());
    }


    @Test
    public void test_that_default_channel_maxage_gets_set() {
        CacheChannelStructure cc = new CacheChannelStructure();
        cc.addCacheControlHeader("max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage, " +
                "group=\"/menu2\", group=\"/menu3\"");

        assertEquals("channel-maxage inherits max-age if not set",
                "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                        "group=\"/menu2\", group=\"/menu3\"",
                cc.toCacheChannelString());
        // Trying without values
        cc = new CacheChannelStructure();
        cc.addCacheControlHeader("max-age=1300, channel=\"http://localhost:9000/atomizer/event/current\", group=\"/menu2\"");
        assertEquals("max-age=1300, channel=\"http://localhost:9000/atomizer/event/current\", " +
                "channel-maxage=1300, group=\"/menu2\"",
                cc.toCacheChannelString());
    }

    @Test
    public void testHugeCC() {
        CacheChannelStructure cc = new CacheChannelStructure(1024);
        cc.addCacheControlHeader(testCacheControl2);
        cc.addCacheControlHeader("group=\"/art1\"");

        String ccString = cc.toCacheChannelString();
        assertTrue("Expecting string to contain too_long group", ccString.contains(CacheChannelStructure.TOO_LONG_GROUP));
        assertTrue(ccString.endsWith(CacheChannelStructure.TOO_LONG_GROUP));

        cc.addCacheControlHeader("group=\"/sec1\"");
        assertEquals(ccString, cc.toCacheChannelString());
    }

    @Test
    public void testNumberOfCleanupAttempts() {
        String s =
                "max-age=300, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=300";

        for (int i = 1; i < 550; i++) {
            s += ", group=\"/art" + i + "\"";
        }

        for (int i = 1; i < 550; i++) {
            s += ", group=\"/foo" + i + "\"";
        }

        CacheChannelStructure cc = new CacheChannelStructure();
        // TODO It is supposed to give trouble: Assert.assertFalse(cc.addCacheControlHeader(s));
    }
}
