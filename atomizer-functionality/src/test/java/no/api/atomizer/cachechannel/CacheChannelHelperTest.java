package no.api.atomizer.cachechannel;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static no.api.atomizer.cachechannel.CacheChannelHelper.isChannelKey;
import static no.api.atomizer.cachechannel.CacheChannelHelper.isChannelMaxAgeKey;
import static no.api.atomizer.cachechannel.CacheChannelHelper.isGroupKey;
import static no.api.atomizer.cachechannel.CacheChannelHelper.isLegalCacheChannelElement;
import static no.api.atomizer.cachechannel.CacheChannelHelper.isMaxAgeKey;
import static no.api.atomizer.cachechannel.CacheChannelHelper.stripId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CacheChannelHelperTest {

    @Test
    public void testConstructor() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            InstantiationException {
        Constructor c = CacheChannelHelper.class.getDeclaredConstructor();
        assertFalse(c.isAccessible());
        c.setAccessible(true);
        c.newInstance();

    }
    @Test
    public void testIsChannelKey() throws Exception {
        assertFalse(isChannelKey(null));
        assertFalse(isChannelKey("foo"));
        assertTrue(isChannelKey(CacheChannelHelper.KEY_CHANNEL));
    }

    @Test
    public void testIsChannelMaxAgeKey() throws Exception {
        assertFalse(isChannelMaxAgeKey(null));
        assertFalse(isChannelMaxAgeKey("foo"));
        assertTrue(isChannelMaxAgeKey(CacheChannelHelper.KEY_CHANNEL_MAX_AGE));
    }

    @Test
    public void testIsMaxAgeKey() throws Exception {
        assertFalse(isMaxAgeKey(null));
        assertFalse(isMaxAgeKey("foo"));
        assertTrue(isMaxAgeKey(CacheChannelHelper.KEY_MAX_AGE_FRONT_END));
    }

    @Test
    public void testIsGroupKey() throws Exception {
        assertFalse(isGroupKey(null));
        assertFalse(isGroupKey("foo"));
        assertTrue(isGroupKey(CacheChannelHelper.KEY_GROUP));
    }

    /*
    @Test
    public void testIsArticleGroupElement() throws Exception {
        Assert.assertFalse(CacheChannelPantheonHelper.isArticleGroupElement(null));
        Assert.assertFalse(CacheChannelPantheonHelper.isArticleGroupElement("foo"));
        Assert.assertTrue(CacheChannelPantheonHelper.isArticleGroupElement("/art1000"));
    }

    @Test
    public void testIsSectionGroupElement() throws Exception {
        Assert.assertFalse(CacheChannelPantheonHelper.isSectionGroupElement(null));
        Assert.assertFalse(CacheChannelPantheonHelper.isSectionGroupElement("foo"));
        Assert.assertTrue(CacheChannelPantheonHelper.isSectionGroupElement("/sec1000"));
    }
    */

    @Test
    public void testIsLegalCacheChannelElement() throws Exception {
        assertFalse(isLegalCacheChannelElement(null));
        assertFalse(isLegalCacheChannelElement(new CacheChannelElement(null, null)));

    }


    @Test
    public void testStripId() throws Exception {
        assertEquals(new Integer(101), stripId("/sec101", "/sec"));
        assertNull(stripId("/sec101", "/se"));
        assertNull(stripId("/sec101s", "/sex"));
        assertNull(stripId(null, null));
    }
}
