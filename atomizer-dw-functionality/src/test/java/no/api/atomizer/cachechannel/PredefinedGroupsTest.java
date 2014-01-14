package no.api.atomizer.cachechannel;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PredefinedGroupsTest {

    @Test
    public void testConstructor() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            InstantiationException {
        Constructor c = PredefinedGroups.class.getDeclaredConstructor();
        Assert.assertFalse(c.isAccessible());
        c.setAccessible(true);
        c.newInstance();
    }

}
