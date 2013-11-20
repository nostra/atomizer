package no.api.atomizer.cachechannel.client;

import no.api.atomizer.cachechannel.CacheControl;
import no.api.atomizer.cachechannel.filter.HttpHeaderScrutinizer;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This class will help you to add CacheChannel headers to a given HttpServletResponse object.
 *
 */
public class CacheChannelHeader {

    private static final long THIS_MANY_MS_IN_A_SECOND = 1000L;

    /**
     * When some errors happen, or there is some trouble.
     */
    private static final int DEFAULT_TROUBLE_MAX_AGE_IN_SECONDS = 300;

    private HttpServletResponse response;

    public CacheChannelHeader(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * @param group a complete reference including group prefix, but without "-signs
     */
    public void addCacheChannelHeader(String group) {
        response.addHeader(CacheControl.CACHE_CONTROL, "group=\"" + group + "\"");
    }

    /**
     * The front end (i.e. browser) max-age
     */
    public void adjustFrontendMaxAgeTo(long ageInSec) {
        response.addHeader(CacheControl.CACHE_CONTROL, "max-age=" + ageInSec);
    }

    public void adjustChannelMaxAgeTo(long ageInSec) {
        response.addHeader(CacheControl.CACHE_CONTROL, "channel-maxage=" + ageInSec);
    }

    /**
     * @deprecated This will change the channel-maxage and not the age element. Use adjustFrontendMaxAgeTo for max-age change
     * @see #adjustFrontendMaxAgeTo(long)
     * @see #adjustChannelMaxAgeTo(long)
     */
    public void adjustMaxAgeTo(long ageInSec) {
        adjustChannelMaxAgeTo(ageInSec);
    }

    public void addCacheChannelHeaderWith300InChannelMaxAge(String group) {
        adjustChannelMaxAgeTo(DEFAULT_TROUBLE_MAX_AGE_IN_SECONDS);
        addCacheChannelHeader(group);
    }

    /**
     * @deprecated This will change the channel-maxage and not the age element. Use adjustFrontendMaxAgeTo for max-age change
     * @see #addCacheChannelHeaderWith300InChannelMaxAge(String)
     */
    public void addCacheChannelHeaderWith300InMaxAge(String group) {
        addCacheChannelHeaderWith300InChannelMaxAge( group );
    }

    /**
     * @param group a complete reference including group prefix, but without "-signs
     */
    public void addMidnightCacheChannelHeader(String group) {
        adjustChannelMaxAgeTo(calculateMidnightPrefix(new Date()));
        addCacheChannelHeader(group);
    }

    protected long calculateMidnightPrefix(Date now) {
        GregorianCalendar nowCal = new GregorianCalendar();
        nowCal.setTime(now);
        GregorianCalendar midnightCal =
                new GregorianCalendar(nowCal.get(GregorianCalendar.YEAR), nowCal.get(GregorianCalendar.MONTH),
                        nowCal.get(GregorianCalendar.DAY_OF_MONTH), 0, 0);
        midnightCal.add(GregorianCalendar.DAY_OF_MONTH, 1);
        long diff = midnightCal.getTimeInMillis() - nowCal.getTimeInMillis();
        return diff / THIS_MANY_MS_IN_A_SECOND;
    }

    public void poisonCacheChannelWith300SecLife(String reason) {
        //use 5 minutes expires and no cache-channel, ref SPR-2974
        performCacheChannelPoisioning( reason, ""+ DEFAULT_TROUBLE_MAX_AGE_IN_SECONDS);
    }

    public void poisonCacheChannelWithExpiresNow(String reason) {
        performCacheChannelPoisioning( reason, "0");
    }
    private void performCacheChannelPoisioning( String reason, String ttlInSecs ) {
        response.addHeader(HttpHeaderScrutinizer.CACHE_CHANNEL_POISON, ttlInSecs );
        response.addHeader(HttpHeaderScrutinizer.X_POISONED_BY, reason);
    }


    public void addNearestChannelHeader(String group, int timeInMinutes) {
        adjustChannelMaxAgeTo(calculateMinuteMaxagePrefix(new Date(), timeInMinutes));
        addCacheChannelHeader(group);
    }

    protected long calculateMinuteMaxagePrefix(Date now, int minutes) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(now);
        cal.set(GregorianCalendar.MILLISECOND, 0);
        cal.set(GregorianCalendar.SECOND, 0);
        int currentMin = cal.get(GregorianCalendar.MINUTE) + 1;
        int i = 1;
        while (currentMin % minutes > 0) {
            i++;
            currentMin++;
        }
        cal.add(GregorianCalendar.MINUTE, i);
        Date then = cal.getTime();
        long diff = then.getTime() - now.getTime();
        return diff / THIS_MANY_MS_IN_A_SECOND;
    }

    public boolean hasExpiresHeader() {
        return response.containsHeader("Expires");
    }

}
