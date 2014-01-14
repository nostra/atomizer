package no.api.atomizer.cachechannel;

import no.api.atomizer.cachechannel.filter.HttpHeaderScrutinizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * We need to have control with both the cache control headers and the
 * age parameter. The rules are as follows:
 *
 * <ul>
 *
 *     <li> If we get cc_maxage and age, we want retain the header which has the lowest value for maxage - age</li>
 *
 *     <li> If we have only maxage, we will presume that age (until it is set, anyway) is zero </li>
 *
 *     <li> Poison,etc, are not handled by this functionality, but outside in the scrutinizer </li>
 *
 *     <li> If we have only age, we want to retain the value in case cc header comes later </li>
 *
 * </ul>
 *
 * This class functions as a proxy for the cache control service.
 *
 * With two pairs of cc maxage and age, you have the following setups:
 *
 * <pre>
 *     ccaa : Last age is discarded
 *     caca : Normal
 *     caac : Normal
 *     acca : Normal
 *     acac : Normal
 *     aacc : First age is discarded, last maxage is assumed to have age=0
 * </pre>
 * @deprecated This version will be superceeded by a version which is not dependant on response
 */
@Deprecated
public class ChannelMaxAgeVersusAgeProxy implements MaxAgeSetter {
    private static final Logger log = LoggerFactory.getLogger(ChannelMaxAgeVersusAgeProxy.class);

    private final HttpServletResponse response;

    private CacheControl cacheControl;

    private AgeAndMaxAgeHolder aama = new AgeAndMaxAgeHolder();

    /**
     * TODO Temporarily protected, due to necessity of inheritance in legacy project.
     */
    protected ChannelMaxAgeVersusAgeProxy(HttpServletResponse response) {
        this.response = response;
    }

    public static ChannelMaxAgeVersusAgeProxy createMaxAgeVersusAge( HttpServletResponse response ) {
        ChannelMaxAgeVersusAgeProxy mava = new ChannelMaxAgeVersusAgeProxy(response);
        mava.cacheControl = new CacheControl(mava, new RegularLowestValueMaxAgeSetter(response));
        return mava;
    }

    public void addAge(int age) {
        aama = aama.adjustAge(age);
        response.setHeader(HttpHeaderScrutinizer.AGE,
                Integer.valueOf(Math.max( aama.calculateMinimal().getAge(), 0)).toString());
    }

    public boolean addElements(String value) {
        return cacheControl.addElements(value);
    }

    public String toCacheChannelString() {
        return cacheControl.toCacheChannelString();
    }

    public boolean isMaintained() {
        return cacheControl.isMaintained();
    }

    @Override
    public int getMaxAge() {
        return aama.calculateMinimal().getMaxAge();
    }

    /**
     * Set the channel-maxage value.
     *
     * TODO Erlend 19.06.2013: Need to change name to channel-maxage
     *
     * <p><i>NOTE</i> that this method has special behaviour. The given value will only be used if max-age is not
     * already set, or if the given value is lower than the one set before. Negative values are not accepted.</p>
     *
     * @param value The new value for channel-maxage that we should attempt to insert.
     */
    public void setMaxAge(int value) {
        aama = aama.adjustMaxage(value);
        response.setHeader(HttpHeaderScrutinizer.AGE,
                Integer.valueOf(Math.max(aama.calculateMinimal().getAge(), 0)).toString());
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
        response.setHeader(HttpHeaderScrutinizer.AGE,"");
        // There may be a problem with this that age should really be reset to the previous value
    }

    public void setMaxAgeByProxy(Integer value) {
        cacheControl.setMaxAge(value);
    }
}
