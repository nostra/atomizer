package no.api.atomizer.cachechannel.filter;

import no.api.atomizer.cachechannel.CacheControl;
import no.api.atomizer.cachechannel.ChannelMaxAgeVersusAgeProxy;
import no.api.atomizer.cachechannel.transform.Date2String;
import no.api.atomizer.cachechannel.transform.String2Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Examine header elements and adjust them for different actions:
 * <p/>
 * <ul>
 * <p/>
 * <li> Discard cookies and Content-Length (which is set later by the character stripper filter, which is (probably)
 * used)</li>
 * <p/>
 * <li> Retain X-Trace-App </li> <li> Retain the value for Expires which has the lowest value</li>
 * <p/>
 * <li> Cache-Control: max-age with the lowest values shall be retained. All other values shall just be appended. </li>
 * <p/>
 * </ul>
 * <p/>
 * See also http://jira.api.no/browse/SPR-2253
 */
public class HttpHeaderScrutinizer extends HttpServletResponseWrapper {

    private static final long THIS_MANY_MS_IN_A_SECOND = 1000L;

    private final Logger log = LoggerFactory.getLogger(HttpHeaderScrutinizer.class);
    /*
    * Notice that the static contents of default max age is replaced with replaceAll in both astl and relax.
    */

    /**
     * LEGACY : Kept for legacy reasons
     *
     * @deprecated Use defaults in CacheControl object instead. No need to set this.
     */
    public static final String DEFAULT_MAX_AGE = "" + CacheControl.DEFAULT_CHANNEL_MAX_AGE;


    /**
     * If this poison pill is received, cache channel header will be set to naught.
     */
    public static final String CACHE_CHANNEL_POISON = "Do_not_use_cache_channel_header";

    /**
     * Use addHeader in order to set this element. It is intended to be a plain text explanation of why poison has been
     * served. NB Only the first is accepted.
     */
    public static final String X_POISONED_BY = "X-Poisoned-By";

    public static final String X_TRACE_APP = "X-Trace-App";

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

    public static final String HEADER_LENGTH = "Header-Length";

    public static final String ETAG = "ETag";

    public static final String AGE = "Age";

    public static final String EXPIRES = "Expires";

    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String SURROGATE_CONTROL = "Surrogate-Control";

    public static final String CONTENT_LANGUAGE = "Content-Language";

    public static final String CONTENT_ENCODING = "Content-Encoding";

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String LOCATION = "Location";

    public static final String VARY = "Vary";

    public static final String SET_COOKIE = "Set-Cookie";

    public static final String P3P = "P3P";

    public static final String X_UA_COMPATIBLE = "X-UA-Compatible";

    public static final String NO_CACHE = "no_cache";

    private static final int EXPIRE_SECONDS_IN_HEADER = 300;

    private Long expireDate = null;

    private ChannelMaxAgeVersusAgeProxy ccAndAgeProxy;

    private StringBuilder currentTraceApp = new StringBuilder();

    private StringBuilder currentVary = new StringBuilder();

    private boolean poisonedByExplanationIsGiven = false;

    public static final String X_FRAME_OPTIONS = "X-Frame-Options";

    public static final String X_FRAME_OPTIONS_SAME_ORIGIN = "Sameorigin";

    public static final String X_FRAME_OPTIONS_DENY = "Deny";

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * Reference to owning class. Only used (and should only be used) for logging. It may be null, in which case, it is
     * disregarded.
     */
    private HttpTraceFilter logging;

    private long bench;

    private String path;

    private boolean allowCookies;

    private boolean disallowAge;

    private Date overrideNewDate = null; // For test purposes. TODO We should fix better tests instead.

    public static final String CACHE_CHANNEL_NO_CACHE_HEADER_CONTENTS = "max-age=0, no-cache, must-revalidate";

    private List<String> validHeaders;

    public static final String STREAM_END_MARKER = "MARK_END";

    public static final String STREAM_START_MARKER = "MARK_START";

    public static final String LINK = "Link";

    public static final String X_ROBOTS_TAG = "X-Robots-Tag";

    public HttpHeaderScrutinizer(HttpServletResponse response, String spath, long bench,
                                 HttpTraceFilter httpTraceFilter, boolean allowCookies) {
        super(response);
        setValidHeaders();
        this.bench = bench;
        this.logging = httpTraceFilter;
        this.path = spath;
        this.allowCookies = allowCookies;
        this.ccAndAgeProxy = ChannelMaxAgeVersusAgeProxy.createMaxAgeVersusAge(response);
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (cookie != null) {
            if (allowCookies) {
                log.debug("Setting cookie " + cookie.getName() + "[" + cookie.getPath() + "]=" + cookie.getValue());
                super.addCookie(cookie);
            } else if (log.isDebugEnabled()) {
                log.debug("Discarding cookie " + cookie.getName() + "[" + cookie.getPath() + "]=" + cookie.getValue());
            }
        }
    }

    @Override
    public void setDateHeader(String name, long date) {
        log.debug("setDateHeader: incoming header element: {}, value: {}", name, date);
        super.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        log.debug("addDateHeader: incoming header element: {}, value: {}", name, date);
        super.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        if (!validHeaders.contains(name)) {
            log.debug("setHeader: incoming header element discarded: {}, value: {}", name, value);
            return; /* Invalid header is scrutinized here */
        }
        log.debug("setHeader: incoming header element: {}, value: {}", name, value);
        if (name.equals(STREAM_START_MARKER)) {
            if ( ccAndAgeProxy != null ) {
                // Could be null due to poison
                ccAndAgeProxy.streamStart();
            }
        } else if (name.equals(STREAM_END_MARKER)) {
            if ( ccAndAgeProxy != null ) {
                // Could be null due to poison
                ccAndAgeProxy.streamStart();
            }
        } else if (name.equals(CACHE_CHANNEL_POISON)) {
            setHeaderCacheChannelPoison(value);
        } else if (name.equals(X_POISONED_BY)) {
            setHeaderXPoisonedBy(value);
        } else if (name.equals(CacheControl.CACHE_CONTROL)) {
            setHeaderCacheControl(value);
        } else if (name.equals(EXPIRES)) {
            setHeaderExpires(value);
        } else if (name.equals(CONTENT_LENGTH)) {
            log.debug("discarding {}", CONTENT_LENGTH);
        } else if (name.equals(CONTENT_TYPE)) {
            setContentType(value);
        } else if (name.equals(VARY)) {
            setHeaderVary(value);
        } else if (name.equals(HttpTraceFilter.LOGGING_META_HEADER)) {
            // Internal method really
            if (logging != null) {
                logging.doTimingLogging(path, value, bench, System.currentTimeMillis());
            }
        } else if (name.equals(SET_COOKIE)) {
            if (allowCookies) {
                super.setHeader(SET_COOKIE, value);
            }
        } else if (name.equals(AGE)) {
            if (! disallowAge ) {
                setAgeViaProxy(value);
            }
        } else {
            super.setHeader(name, value);
        }
    }

    private void setHeaderCacheChannelPoison(String value) {
        if (value.equals(NO_CACHE)) {
            log.debug("Got poison pill with no_cache option. Serving poison and resetting expire and setting " +
                    "cache-control to no-cache, must-revalidate");
            poisonCacheChannelWithExpires(0, true);
            super.setHeader(EXPIRES, Date2String.transformDateToRfc1123String(createNewDate()));
            // Notice that we do not set no-store. The reason for this is as it is a directive for the browser,
            super.setHeader(CacheControl.CACHE_CONTROL, CACHE_CHANNEL_NO_CACHE_HEADER_CONTENTS);
        } else {
            try {
                poisonCacheChannelWithExpires(Integer.parseInt(value), true);
            } catch (NumberFormatException ignored) {
                poisonCacheChannelWithExpires(EXPIRE_SECONDS_IN_HEADER, true);
            }
            log.debug("Cache-Channel poisoned, and expire header is set.");
        }
    }

    private void setHeaderXPoisonedBy(String value) {
        if (poisonedByExplanationIsGiven) {
            log.debug("Poisoned by explanation is already given; Not setting another (which was {})", value);
        } else {
            poisonedByExplanationIsGiven = true;
            super.setHeader(X_POISONED_BY, value);
        }
    }

    private void setHeaderVary(String value) {
        for (String v : value.split(",")) {
            if (currentVary.indexOf(v) < 0) {
                if (currentVary.length() > 0) {
                    currentVary.append(",");
                }
                currentVary.append(v);
                super.setHeader(VARY, currentVary.toString());
            }
        }
    }

    private void setHeaderExpires(String value) {
        // Notice double loop of this method if cache channel has not been poised yet
        if (ccAndAgeProxy != null) {
            try {
                poisonCacheChannelWithExpires(Integer.parseInt(value), false);
            } catch (NumberFormatException ignored) {
                poisonCacheChannelWithExpires(EXPIRE_SECONDS_IN_HEADER, false);
            }
            setExpireIfLowerThanExisting(value);
            setHeader(X_POISONED_BY, "Encountered Expires-field, which has precedence over cache control header.");
        } else {
            setExpireIfLowerThanExisting(value);
        }
    }

    private void setHeaderCacheControl(String value) {
        if (ccAndAgeProxy == null) {
            log.debug("Ignoring cache channel request, as poison has been served.");
        } else {
            if (!ccAndAgeProxy.addElements(value)) {
                poisonCacheChannelWithExpires(EXPIRE_SECONDS_IN_HEADER, true);
                setHeader(X_POISONED_BY, "Exceeded max field length of cc header field.");
            } else {
                updateCacheControlHeaderIfApplicable();
            }
        }
    }

    private void updateCacheControlHeaderIfApplicable() {
        if ( ccAndAgeProxy == null ) {
            return; // Poisoned
        }

        String cs = ccAndAgeProxy.toCacheChannelString();
        if (cs != null) {
            super.setHeader(CacheControl.CACHE_CONTROL, cs);
            if (ccAndAgeProxy.isMaintained()) {
                setHeader(X_POISONED_BY,
                        "Exceeded max field length of cc header field, but is maintained.");
            }
        } else {
            log.error("CacheControl failed to do its job. Please fix code. " +
                    "Setting a poison pill to prevent major errors.");
            poisonCacheChannelWithExpires(EXPIRE_SECONDS_IN_HEADER, true);
            setHeader(X_POISONED_BY, "Serious trouble with the CacheControl logic. " +
                    "This must be looked into as soon as possible!");
        }
    }

    /**
     * @param expiresInSeconds Lifetime in seconds
     * @param addExpiresHeader whether the expires header argument shall be used.
     */
    private void poisonCacheChannelWithExpires(int expiresInSeconds, boolean addExpiresHeader) {
        ccAndAgeProxy = null;
        if (expiresInSeconds < 1) {
            log.debug("Poisoning the cache channel with no-cache, etc. Expiry header will " +
                    (addExpiresHeader ? "" : "NOT") + " be used");
            super.setHeader(CacheControl.CACHE_CONTROL, CACHE_CHANNEL_NO_CACHE_HEADER_CONTENTS);
        } else {
            // TODO Should really choose the maximum age here:
            log.debug("Poisoning the cache channel with max-age=" + expiresInSeconds + ". Expiry header will " +
                    (addExpiresHeader ? "" : "NOT") + " be used");
            super.setHeader(CacheControl.CACHE_CONTROL, "max-age=0, channel-maxage="+expiresInSeconds);
        }
        //the following code does not work in resin, because resin does not accept null values.
        // It is desirable though, because null values remove headers in other appservers (jetty :) ).
        /* try {
            super.setHeader(CACHE_CONTROL, null);
        } catch (IllegalArgumentException iae) {
            log.error("Exception ({}) is ignored, as it is expected in junit tests." +
                    " Please notify if this exception is found in production.", iae.getMessage());
            // Seems like spring mock likes empty string better
            super.setHeader(CACHE_CONTROL, "");
        }*/
        if (addExpiresHeader) {
            //try obtaining expires time in seconds from value
            long inFuture = createNewDate().getTime() + (expiresInSeconds * THIS_MANY_MS_IN_A_SECOND);
            setHeader(EXPIRES, Date2String.transformDateToRfc1123String(new Date(inFuture)));
        }
    }

    private void setExpireIfLowerThanExisting(String value) {
        long dt = String2Date.transformStringDate(value, -1);
        if (dt == -1) {
            log.warn("Could not transform date: {}", value);
            return;
        }
        if (expireDate == null || dt < expireDate.longValue()) {
            expireDate = Long.valueOf(dt);
            super.setHeader(EXPIRES, value);
        }
    }

    private void setAgeViaProxy(String value) {
        if ( ccAndAgeProxy == null ) {
            return; // Poisoned
        }
        int iv = 0;
        try {
            iv = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Could not transform value when setting Age header: {}", value);
            return;
        }
        ccAndAgeProxy.addAge(iv);
        updateCacheControlHeaderIfApplicable(); // Need to update CC header too if applicable
    }

    @Override
    public void addHeader(String name, String value) {
        if (name.equals(X_TRACE_APP)) {
            if (currentTraceApp.indexOf(xtraceAppUntilTimestamp(value)) < 0) {
                // Trying to avoid duplications
                currentTraceApp.append(value);
                super.addHeader(name, value);
            }
        } else if (name.equals(CONTENT_TYPE)) {
            setContentType(value);
        } else if (name.equals(VARY)) {
            for (String v : value.split(",")) {
                // NB: Using setheader in order to have only one
                setHeader(name, v);
            }
        } else if (name.equals(X_POISONED_BY)) {
            if (poisonedByExplanationIsGiven) {
                log.debug("Poisoned by explanation is already given; Not setting another (which was {})", value);
            } else {
                super.addHeader(name, value);
            }
        } else if (name.equals(SET_COOKIE)) {
            if (allowCookies) {
                super.addHeader(SET_COOKIE, value);
            }
        } else {
            setHeader(name, value); /* Set header will filter out only valid headers. It will also ensure that it's added only once */
        }
    }

    private void setValidHeaders() {
        validHeaders = new ArrayList<String>();
        validHeaders.add(X_TRACE_APP);
        validHeaders.add(CACHE_CHANNEL_POISON);
        validHeaders.add(CacheControl.CACHE_CONTROL);
        validHeaders.add(EXPIRES);
        validHeaders.add(CONTENT_LENGTH);
        validHeaders.add(CONTENT_DISPOSITION);
        validHeaders.add(CONTENT_TYPE);
        validHeaders.add(VARY);
        validHeaders.add(CONTENT_LANGUAGE);
        validHeaders.add(CONTENT_ENCODING);
        validHeaders.add(SURROGATE_CONTROL);
        validHeaders.add(X_POISONED_BY);
        validHeaders.add(HttpTraceFilter.LOGGING_META_HEADER);
        validHeaders.add(ETAG);
        validHeaders.add(SET_COOKIE);
        validHeaders.add(X_UA_COMPATIBLE);
        validHeaders.add(X_FRAME_OPTIONS);
        validHeaders.add(P3P);
        validHeaders.add(LOCATION);
        validHeaders.add(ACCESS_CONTROL_ALLOW_ORIGIN);
        validHeaders.add(ACCESS_CONTROL_ALLOW_CREDENTIALS);
        validHeaders.add(ACCESS_CONTROL_ALLOW_HEADERS);
        validHeaders.add(AGE);
        validHeaders.add(STREAM_START_MARKER);
        validHeaders.add(STREAM_END_MARKER);
        validHeaders.add(LINK);
        validHeaders.add(X_ROBOTS_TAG);
    }
    
    /**
     * Helper method which removes the timestamp part of the X-Trace-App header item.
     *
     * @param value Expected to be an x-trace-app element, for instance <code>[relax ; verksam.api.kunder.linpro.no ;
     *              Thu Feb 03 10:42:55 CET 2011]</code>
     * @return Text until the second semi-colon.
     */
    private String xtraceAppUntilTimestamp(String value) {
        if (value == null) {
            return null;
        }
        int lastSemiColon = value.lastIndexOf(';');
        if (lastSemiColon < 0) {
            return value;
        }
        return value.substring(0, lastSemiColon);
    }

    @Override
    public void setIntHeader(String name, int value) {
        log.debug("setIntHeader: incoming header element: {}, value: {}", name, value);
        super.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        log.debug("addIntHeader: incoming header element: {}, value: {}", name, value);
        super.addIntHeader(name, value);
    }

    @Override
    public void setContentType(String type) {
        if (getContentType() != null && log.isDebugEnabled()) {
            log.debug("Exchanging previously set content type: " + getCharacterEncoding() + " with " + type);
        }
        super.setContentType(type);
    }

    /**
     * Adjusting max age element on cache control object.
     */
    public void adjustDefaultCacheChannelMaxAge(Integer defaultCacheChannelMaxAge) {
        if ( ccAndAgeProxy == null ) {
            return; // Poisoned
        }
        ccAndAgeProxy.setMaxAge(defaultCacheChannelMaxAge.intValue());
    }

    private Date createNewDate() {
        if (overrideNewDate != null) {
            return overrideNewDate;
        }
        return new Date();
    }

    public void setDisallowAge(boolean disallowAge) {
        this.disallowAge = disallowAge;
    }

    public void adjustDefaultMaxAge(Integer value) {
        if ( ccAndAgeProxy != null ) {
            ccAndAgeProxy.setMaxAgeByProxy(value);
        }

    }
}