package no.api.atomizer.header.filter;

import no.api.atomizer.header.filter.internal.AddOrSetHeaderProxy;
import no.api.atomizer.header.filter.internal.HeaderElement;
import no.api.atomizer.header.filter.internal.SetOrAddEnum;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class AddOrSetHeaderProxyTest {

    @Test
    public void testSetHeader() {
        AddOrSetHeaderProxy h = new AddOrSetHeaderProxy();
        assertEquals(0, h.size());
        h.putHeader(new HeaderElement("a", "dummy", SetOrAddEnum.SET));
        h.putHeader(new HeaderElement("b", "dummy", SetOrAddEnum.ADD));
        h.putHeader(new HeaderElement("c", "dummy", SetOrAddEnum.ADD));
        h.putHeader(new HeaderElement("c", "dummy", SetOrAddEnum.ADD));
        assertEquals(4, h.size());
        h.putHeader(new HeaderElement("a", "dummy", SetOrAddEnum.SET));
        assertEquals(4, h.size());
    }
}
