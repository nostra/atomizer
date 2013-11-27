package no.api.atomizer.header.structure;

import no.api.atomizer.cachechannel.AgeAndMaxAgeHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ChannelMaxAgeSetter {
    private static final Logger log = LoggerFactory.getLogger(ChannelMaxAgeSetter .class);

    private AgeAndMaxAgeHolder aama = new AgeAndMaxAgeHolder();

    public void addAge(int age) {
        aama = aama.adjustAge(age);
        //response.setHeader(HttpHeaderScrutinizer.AGE,
        // TODO Add where used:        Integer.valueOf(Math.max( aama.calculateMinimal().getAge(), 0)).toString());
    }

    public int getChannelMaxAge() {
        return aama.calculateMinimal().getMaxAge();
    }

    /**
     * Set the channel-maxage value.
     *
     * <p><i>NOTE</i> that this method has special behaviour. The given value will only be used if max-age is not
     * already set, or if the given value is lower than the one set before. Negative values are not accepted.</p>
     *
     * @param value The new value for channel-maxage that we should attempt to insert.
     */
    public void setMaxAge(int value) {
        aama = aama.adjustMaxage(value);
        //response.setHeader(HttpHeaderScrutinizer.AGE,
        // TODO Add where used:                Integer.valueOf(Math.max(aama.calculateMinimal().getAge(), 0)).toString());
    }

    /**
     * Director (most likely) informs that transmission has started
     */
    public void streamStart() {
        bump();
    }

    /**
     * Director (most likely) informs that transmission has started
     */
    public void streamEnd() {
        bump();
    }

    /**
     * Start and end are working in the same way at present. Using the same method
     */
    private void bump() {
        AgeAndMaxAgeHolder minimal = aama.calculateMinimal();
        if ( minimal.getAge() != -1 && minimal.getMaxAge() == -1 ) {
            log.debug("Discarding age of "+minimal.getAge()+" because it is alone (i.e. without cc max-age)");
            resetAge();
        }
    }

    private void resetAge() {
        aama = aama.resetAge();
        // Would have preferred to have used null here, but escenic does not manage it.
        // TODO response.setHeader(HttpHeaderScrutinizer.AGE,"");
        // There may be a problem with this that age should really be reset to the previous value
    }
}
