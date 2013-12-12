package no.api.atomizer.resources;

import org.junit.Assert;
import no.api.atomizer.core.StaleGroup;
import no.api.atomizer.views.beans.GuiEntry;
import org.junit.Test;

/**
 *
 */
public class AbstractAtomizerResourceTest {

    @Test
    public void testTransformToGuiEntry() {
        JunitAtomizerResource resource = new JunitAtomizerResource();
        StaleGroup sg = new StaleGroup();
        sg.setPath("/junit");
        sg.setUpdated(System.currentTimeMillis());
        sg.setId("inconsequential");
        GuiEntry transformed = resource.transformToGuiEntry(sg);
        Assert.assertEquals(sg.getPath(), transformed.getLinks().get(0));
        Assert.assertEquals(sg.getUpdated(), transformed.getUpdated().getTime());

    }

    private class JunitAtomizerResource extends AbstractAtomizerResource{
        public JunitAtomizerResource() {
            super(null);
        }
    }
}
