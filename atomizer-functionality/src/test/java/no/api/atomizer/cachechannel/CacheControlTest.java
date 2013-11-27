package no.api.atomizer.cachechannel;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class CacheControlTest {

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
    public void testAddElements() {
        CacheControl cc = new CacheControl();
        if (cc.addElements(testCacheControl)) {
            Assert.assertEquals(1400, cc.getMaxAge());
            Assert.assertEquals(1400, cc.getChannelMaxAge());

        } else {
            Assert.fail("We expected addElements to not Assert.fail");
        }
    }

    @Test
    public void testDefaultMaxAge() {
        CacheControl cc = new CacheControl(ChannelMaxAgeVersusAgeProxy.createMaxAgeVersusAge(null), new RegularLowestValueMaxAgeSetter(null));
        Assert.assertEquals(CacheControl.DEFAULT_CHANNEL_MAX_AGE, cc.getChannelMaxAgeOrDefault());
        Assert.assertEquals(CacheControl.DEFAULT_MAX_AGE, cc.getMaxAgeOrDefault());
    }

    @Test
    public void testResettingMaxAge() {
        CacheControl cc = new CacheControl();
        cc.setMaxAge(0);
        Assert.assertEquals(0, cc.getMaxAgeOrDefault());
    }

    @Test
    public void testResettingMaxAgeExpr() {
        CacheControl cc = new CacheControl();
        cc.setMaxAge(3210);

        Assert.assertTrue(cc.toCacheChannelString().contains("max-age=3210"));
    }

    @Test
    public void testSetMaxAge() {
        CacheControl cc = new CacheControl(ChannelMaxAgeVersusAgeProxy.createMaxAgeVersusAge(new MockHttpServletResponse()), new RegularLowestValueMaxAgeSetter(new MockHttpServletResponse()));
        cc.setMaxAge(-1);
        Assert.assertEquals(-1, cc.getMaxAge());
        cc.setMaxAge(8000);
        Assert.assertEquals(8000, cc.getMaxAge());
        cc.setMaxAge(9000);
        Assert.assertEquals(8000, cc.getMaxAge());
        cc.setMaxAge(6000);
        Assert.assertEquals(6000, cc.getMaxAge());
    }

    @Test
    public void testSetChannel() {
        CacheControl cc = new CacheControl();
        cc.setChannel(null);
        Assert.assertNotNull(cc.getChannel());
        cc.setChannel("channel");
        Assert.assertEquals("channel", cc.getChannel());
    }

    @Test
    public void testAddElement() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CacheControl cc = new CacheControl();

        Method addElementMethod = cc.getClass().getDeclaredMethod("addElement", CacheChannelElement.class);
        addElementMethod.setAccessible(true);

        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("fjom", "")));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("fjom", "")));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("group", null)));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("max-age", "342s")));
        Assert.assertTrue((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("group", "/art12d")));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("group", "/art12d")));
        Assert.assertTrue((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("group", "/secX")));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("group", "/secX")));
        Assert.assertTrue((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("group", "/sec10")));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("group", "/sec10")));

        Method containsSectionsMethod = cc.getClass().getDeclaredMethod("containsSections");
        containsSectionsMethod.setAccessible(true);
        Assert.assertTrue((Boolean) containsSectionsMethod.invoke(cc));

        Assert.assertTrue((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("group", "/menu10")));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement(null, "/menu10")));
        CacheChannelElement cacheChannelElement = null;
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, cacheChannelElement));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement(CacheChannelHelper.KEY_NO_CACHE, "/menu10")));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("group", "/menu10")));
        Assert.assertFalse((Boolean) addElementMethod.invoke(cc, new CacheChannelElement("must-revalidate", null)));
    }

    @Test
    public void testToStringNull() {
        CacheControl cc = new CacheControl(10, null);
        String testString = "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                "group=\"/art2\", group=\"/art1\", group=\"/art3\", group=\"/art4\"";
        cc.addElements(testString);
        Assert.assertNull(cc.toCacheChannelString());
    }

    @Test
    public void testAddElementsInGeneral() {
        CacheControl cc = new CacheControl();
        Assert.assertTrue(cc.addElements(null));
    }

    @Test
    public void testThatTheCorrectArticleIsReplacedWhenMaxFieldLengthExceeded() {
        String testString = "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                "group=\"/art2\", group=\"/art1\", group=\"/art3\", group=\"/art4\"";
        CacheControl cc = new CacheControl(testString.length(), new MockHttpServletResponse());
        Assert.assertFalse(cc.isMaintained());
        cc.addElements(testString);
        if (cc.addElements("group=\"/art5\"")) {
            Assert.assertEquals(
                    "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=300, " +
                            "group=\"/art2\", group=\"/art3\", group=\"/art4\", group=\"/art5\"",
                    cc.toCacheChannelString());
        } else {
            Assert.fail("Expected element to be replaced.");
        }
        Assert.assertTrue(cc.isMaintained());
    }

    @Test
    public void testThatTheCorrectSectionIsReplacedWhenMaxFieldLengthExceeded() {
        String testString = "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                "group=\"/sec2\", group=\"/sec1\", group=\"/sec3\", group=\"/sec4\"";
        CacheControl cc = new CacheControl(testString.length(), new MockHttpServletResponse());
        cc.addElements(testString);
        if (cc.addElements("group=\"/sec5\"")) {
            Assert.assertEquals(
                    "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=300, " +
                            "group=\"/sec2\", group=\"/sec3\", group=\"/sec4\", group=\"/sec5\"",
                    cc.toCacheChannelString());
        } else {
            Assert.fail("Expected element to be replaced.");
        }
    }

    @Test
    public void testThatArticleIsRemovedBeforeSection() throws NoSuchFieldException, IllegalAccessException {
        String testString = "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                "group=\"/sec2\", group=\"/sec1\", group=\"/art100\", group=\"/sec3\"";
        CacheControl cc = new CacheControl(testString.length(), new MockHttpServletResponse());

        // Little bit of reflection magic to enable the test to do its stuff
        Field c1 = cc.getClass().getDeclaredField("articleGroups");
        c1.setAccessible(true);
        Field c2 = cc.getClass().getDeclaredField("sectionGroups");
        c2.setAccessible(true);
        cc.addElements(testString);

        if (cc.addElements("group=\"/sec5\"")) {
            List<Integer> articleList = (List<Integer>) c1.get(cc);
            Collections.sort(articleList);
            List<Integer> sectionList = (List<Integer>) c2.get(cc);
            Collections.sort(sectionList);
            Assert.assertEquals(
                    "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=300, " +
                            "group=\"/sec1\", group=\"/sec2\", group=\"/sec3\", group=\"/sec5\"",
                    cc.toCacheChannelString());
        } else {
            Assert.fail("Expected element to be replaced.");
        }
    }

    @Test
    public void testThatOtherGroupsAreNotReplacedByArticle() {
        String testString = "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                "group=\"/menu2\", group=\"/menu1\", group=\"/menu100\", group=\"/menu3\"";
        CacheControl cc = new CacheControl(testString.length(), new MockHttpServletResponse());
        cc.addElements(testString);
        if (cc.addElements("group=\"/art1\"")) {
            Assert.assertEquals(
                    "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=300, " +
                            "group=\"/menu2\", group=\"/menu1\", group=\"/menu100\", group=\"/menu3\"",
                    cc.toCacheChannelString());

        } else {
            Assert.fail("Something went wrong.");
        }
    }

    @Test
    public void test_that_default_channel_maxage_gets_set() {
        CacheControl cc = new CacheControl(ChannelMaxAgeVersusAgeProxy.createMaxAgeVersusAge(new MockHttpServletResponse()), new RegularLowestValueMaxAgeSetter(new MockHttpServletResponse()));
        cc.addElements("max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage, " +
                        "group=\"/menu2\", group=\"/menu3\"");

        Assert.assertEquals(
                "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage="+CacheControl.DEFAULT_CHANNEL_MAX_AGE+", " +
                        "group=\"/menu2\", group=\"/menu3\"",
                cc.toCacheChannelString());
        // Trying without values
        cc = new CacheControl(ChannelMaxAgeVersusAgeProxy.createMaxAgeVersusAge(new MockHttpServletResponse()), new RegularLowestValueMaxAgeSetter(new MockHttpServletResponse()));
        cc.addElements("max-age=1300, channel=\"http://localhost:9000/atomizer/event/current\", group=\"/menu2\"");
        Assert.assertEquals("max-age=1300, channel=\"http://localhost:9000/atomizer/event/current\", " +
                "channel-maxage="+ CacheControl.DEFAULT_CHANNEL_MAX_AGE+", group=\"/menu2\"",
                cc.toCacheChannelString());
    }

    @Test
    public void testHugeCC() {
        CacheControl cc = new CacheControl(testCacheControl2.length(), new MockHttpServletResponse());
        cc.addElements(testCacheControl2);
        if (cc.addElements("group=\"/art1\"")) {
            Assert.assertEquals(testCacheControl2, cc.toCacheChannelString());

        } else {
            Assert.fail("Something went wrong.");
        }

        if (cc.addElements("group=\"/sec1\"")) {
            Assert.assertNotSame(testCacheControl2, cc.toCacheChannelString());

        } else {
            Assert.fail("Something went wrong.");
        }
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

         CacheControl cc = new CacheControl();
         Assert.assertFalse(cc.addElements(s));
    }

    @Test
    public void testThatOtherGroupsAreNotReplacedBySection() {
        String testString = "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=1400, " +
                "group=\"/menu2\", group=\"/menu1\", group=\"/menu100\", group=\"/menu3\"";
        CacheControl cc = new CacheControl(testString.length(), new MockHttpServletResponse());
        cc.addElements(testString);
        if (cc.addElements("group=\"/sec10\"")) {
            Assert.assertEquals(
                    "max-age=1400, channel=\"http://localhost:9000/atomizer/event/current\", channel-maxage=300, " +
                            "group=\"/menu2\", group=\"/menu1\", group=\"/menu100\", group=\"/menu3\"",
                    cc.toCacheChannelString());

        } else {
            Assert.fail("Something went wrong.");
        }
    }

}
