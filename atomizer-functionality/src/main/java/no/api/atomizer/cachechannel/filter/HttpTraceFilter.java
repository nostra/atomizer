package no.api.atomizer.cachechannel.filter;

import no.api.atomizer.cachechannel.CacheControl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * The purpose of this filter, is to remove elements that are superfluous, and set the elements that we would like to
 * add. In short, we would like to set X-Trace-App elements, and choose the smallest available value for cache timeout
 * values
 *
 * <p> Ref: http://jira.api.no/browse/SPR-2253 </p>
 *
 * <p> Notice that headers are scrutinized, and that a default vary header can be set. </p>
 *
 * @see HttpHeaderScrutinizer
 */
public class HttpTraceFilter implements Filter {

    public static final String INIT_PARAM_DEFAULT_CC_MAX_AGE = "defaultCacheChannelMaxAge";

    public static final String INIT_PARAM_DEFAULT_FRONTEND_MAX_AGE = "defaultMaxAge";

    private static final Logger log = LoggerFactory.getLogger(HttpTraceFilter.class);

    private String webappName;

    private String defaultVary;

    private String surrogateControl;

    private boolean doExtraLogging;

    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    public static final String ACCEPT_LANGUAGE = "Accept-Language";

    public static final String USER_AGENT = "User-Agent";

    /**
     * Header can be added in order to get more timing information logged. When using this header, the scrutinizer must
     * be true.
     */
    public static final String LOGGING_META_HEADER = "Internal-Logging";

    private String localHostName = "unknown";

    /*
     Whether or not the HttpHeaderScrutinizer shall be used. If it is, it will
     trim away header elements that are not used in the solution. Default is true
     i.e. that scrutinizer is used.
    */

    private boolean shallScrutinze = true;

    private boolean disallowAge = false;

    private String ndcInfo = null;

    private boolean allowCookies;

    /**
     * Implemented upon request. See http://jira.api.no/browse/TASK-2710 for more info.
     */
    private String hostnameWebappHeader = null;

    /**
     * Configurable cachechannel-maxage to be sent to scrutinizer
     */
    private Integer defaultCacheChannelMaxAge = null;

    /**
     * Configurable max-age element to be sent to scrutinizer
     */
    private Integer defaultMaxAge = null;

    /**
     * UA Compatibility mode to force IE into submission
     */
    private String xUaCompatible = null;

    public static final String INIT_PARAM_DISALLOW_AGE = "disallowAge";

    private ServletContext servletContext;

    public void init(FilterConfig filterConfig) throws ServletException {
        log.debug("init");
        this.servletContext = filterConfig.getServletContext();
        this.webappName = filterConfig.getInitParameter("webappName");
        if (this.webappName == null) {
            throw new ServletException("Configuration parameter \"webappName\" missing.");
        }

        //
        // Default Vary
        addDefaultVary(filterConfig);

        // Surrogate control header
        addSurrogateControl(filterConfig);

        addDefaultCacheChannelMaxAge(filterConfig);

        addDefaultFrontEndMaxAge(filterConfig);

        shallAgeBeDisallowed( filterConfig );

        //
        // Scrutinizer
        String scrutinze = filterConfig.getInitParameter("shallScrutinze");
        if (scrutinze != null && "false".equalsIgnoreCase(scrutinze)) {
            log.warn("Turning off scrutinze filter. Only the headers will be set by this config. Configuration " +
                    "such as channel max-age will disappear also - if set.");
            this.shallScrutinze = false;
        }

        // Allow cookies
        String shallAllowCookies = filterConfig.getInitParameter("allowCookies");
        if (shallAllowCookies != null && "true".equalsIgnoreCase(shallAllowCookies)) {
            log.info("Allowing cookies also when scrutinizer is turned on.");
            this.allowCookies = true;
        }

        try {
            localHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("Could not resolve local host. Will use " + localHostName + " as default.");
        }

        //
        // Timing information logging
        String logging = filterConfig.getInitParameter("doExtraLogging");
        if ("true".equals(logging)) {
            doExtraLogging = true;
            log.info("Timing information will be logged in this class at log level DEBUG.");
        } else {
            log.info("Logging of timing information is not set or is false. No extra logging will be performed.");
        }

        //
        // Try to add agent to http calls
        addAgentToHttpCalls(filterConfig);

        //
        // Set X-UA-Compatible
        this.xUaCompatible = filterConfig.getInitParameter("xUaCompatible");
        if ( this.xUaCompatible == null || "".equals(this.xUaCompatible) ) {
            log.warn( "param-value missing from xUaCompatible parameter in web.xml. No X-UA-Compatible header will be set.");
        }

        
    }

    private void shallAgeBeDisallowed(FilterConfig filterConfig) {
        String doNotUseAge = filterConfig.getInitParameter(INIT_PARAM_DISALLOW_AGE);
        if ( doNotUseAge != null && doNotUseAge.equalsIgnoreCase("true")) {
            log.info("The http header Age is disallowed and will not be propagated.");
            this.disallowAge = true;
        } else {
            log.info("HTTP-header Age will be propagated. This is the normal behavior setting. If you for some" +
                    "reason need to deviate (for example if you are corredor), you can use "+INIT_PARAM_DISALLOW_AGE);
        }
    }

    private void addDefaultCacheChannelMaxAge(FilterConfig filterConfig) {
        String cc = filterConfig.getInitParameter(INIT_PARAM_DEFAULT_CC_MAX_AGE);
        if (cc == null || cc.trim().length() < 1) {
            log.info("Not adding default cache channel-maxage, as the configuration element \""+
                    INIT_PARAM_DEFAULT_CC_MAX_AGE+"\" is not set. Will use default, which is " +
                    CacheControl.DEFAULT_CHANNEL_MAX_AGE);
        } else {
            try {
                defaultCacheChannelMaxAge = Integer.valueOf(cc);
                log.info("Will use " + defaultCacheChannelMaxAge+
                        " as default cache channel max age.");
                if ( defaultCacheChannelMaxAge < 300 ) {
                    log.warn("WARNING: Nothing that passes through this filter will be cached for more than "+
                            defaultCacheChannelMaxAge+
                            " seconds by varnish, as channel-maxage never will be larger. Use with caution.");
                }
            } catch (NumberFormatException ignored) {
                log.error("Could not use configuration parameter defaultCacheChannelMaxAge:"+cc
                        +" as it is not a number.");
            }
        }
    }

    private void addDefaultFrontEndMaxAge(FilterConfig filterConfig) {
        String cc = filterConfig.getInitParameter(INIT_PARAM_DEFAULT_FRONTEND_MAX_AGE);
        if (cc == null || cc.trim().length() < 1) {
            log.info("Not adding default max-Age, as the configuration element \""+
                    INIT_PARAM_DEFAULT_FRONTEND_MAX_AGE+"\" is not set. Will use default value of " +
                    CacheControl.DEFAULT_MAX_AGE);
        } else {
            try {
                defaultMaxAge = Integer.valueOf(cc);
                log.info("Will use " + defaultMaxAge+
                        " as default max age.");
            } catch (NumberFormatException ignored) {
                log.error("Could not use configuration parameter "+INIT_PARAM_DEFAULT_FRONTEND_MAX_AGE+":"+cc
                        +" as it is not a number.");
            }
        }
    }

    private void addSurrogateControl(FilterConfig filterConfig) {
        this.surrogateControl = filterConfig.getInitParameter("surrogateControl");
        if (this.surrogateControl == null || this.surrogateControl.trim().length() < 1) {
            log.info("Not adding SurrogateControl header, as configuration element \"surrogateControl\" is not set. " +
                    "Example value is \"ESI/1.0\"");
            this.surrogateControl = "";
        } else {
            log.info("Will add " + HttpHeaderScrutinizer.SURROGATE_CONTROL + ": " + surrogateControl +
                    " to each request for which this filter is active.");
        }
    }

    /**
     * Notice that defaultvary used to be ACCEPT_LANGUAGE + "," + USER_AGENT. It is not, anymore.
     */
    private void addDefaultVary(FilterConfig filterConfig) {
        this.defaultVary = filterConfig.getInitParameter("defaultVary");
        if (this.defaultVary == null) {
            this.defaultVary = "";
            log.debug("As web.xml has not configured defaultVary for HttpTraceFilter, default is being used, which is empty: " +
                    this.defaultVary);
        }
        if (defaultVary.length() > 0) {
            log.info("The vary header instructs varnish which header fields to consider, and is configured to " +
                    defaultVary + ".");
        } else {
            log.info("Filter will not supply its own vary header.");
        }
    }

    private void addAgentToHttpCalls(FilterConfig filterConfig) {
        Class packageClazz = null;
        String packageClassName = filterConfig.getInitParameter("packageClass");
        String agentName = localHostName + ":" + webappName;
        if (packageClassName != null) {
            try {
                packageClazz = Class.forName(packageClassName);
                if (packageClazz != null) {
                    agentName += "";
                }
            } catch (Exception e) { // NOSONAR Acceptable here.
                log.error("Package class wrongly configured. Fallback to agent: " + agentName +
                        ". Got masked exception: " + e);
            }
        }
        try {
            System.setProperty("http.agent", agentName);
        } catch (SecurityException e) {
            log.error("Prevented from setting http.agent as system property. " +
                    "Probably due to security settings. Ignoring it. Masked exception: " +
                    agentName);
        }
        addNDC(filterConfig, packageClazz);
    }

    private void addNDC(FilterConfig filterConfig, Class packageClazz) {
        String doNdc = filterConfig.getInitParameter("doNdc");
        if ( !"false".equalsIgnoreCase(doNdc)) {
            try {
                this.ndcInfo =
                        webappName ;
                log.info("Will try to set " + ndcInfo + " on each request.");
            } catch (Exception e) { // NOSONAR Acceptable here.
                log.error("Could not add NDC information string. Got masked exception: " + e);
            }
        } else {
            log.info("Not adding NDC as filter element, " +
                    "as init parameter doNdc was set to false");
        }
    }

    public void destroy() {
        log.debug("destroy");
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        //log.debug("Start of headerfilter >>> Default charset (for platform "+ Charset.defaultCharset().displayName()+")");
        long bench = System.currentTimeMillis();
        HttpServletResponse response = (HttpServletResponse) resp; // NOSONAR Erlend says this is OK
        HttpServletRequest request = (HttpServletRequest) req;
        final String spath = request.getRequestURI();
        /*
            log.debug("--------------- *-PathTranslated: "+request.getPathTranslated()+
                        "\nContextPath: "+request.getContextPath()+
                        "\nPathInfo: "+request.getPathInfo()+
                        "\nRequestURI: "+request.getRequestURI()+
                        "\nServletPath(): "+request.getServletPath()+
                        "\nRequestURL(): "+request.getRequestURL()
        );*/

        String sinfo = "[" + webappName + " ; " + localHostName + " ; " + new Date().toString() + "]";
        response.addHeader(HttpHeaderScrutinizer.X_TRACE_APP, sinfo);
        if (!defaultVary.equals("")) {
            response.addHeader(HttpHeaderScrutinizer.VARY, defaultVary);
        }
        if (!surrogateControl.equals("")) {
            response.addHeader(HttpHeaderScrutinizer.SURROGATE_CONTROL, surrogateControl);
        }
        if (xUaCompatible != null && !"".equals(xUaCompatible)) {
            response.addHeader(HttpHeaderScrutinizer.X_UA_COMPATIBLE, xUaCompatible);
        }
        if (ndcInfo != null) {
            MDC.put("app", ndcInfo);
        }
        try {
            if (shallScrutinze) {
                HttpHeaderScrutinizer scrutinizer =
                        new HttpHeaderScrutinizer(response, spath, bench, this, allowCookies);
                if ( disallowAge ) {
                    log.trace("Age is not going to be propagated in http response");
                    scrutinizer.setDisallowAge(true);
                }
                if ( defaultCacheChannelMaxAge != null ) {
                    scrutinizer.adjustDefaultCacheChannelMaxAge( defaultCacheChannelMaxAge );
                }
                if ( defaultMaxAge != null ) {
                    scrutinizer.adjustDefaultMaxAge( defaultMaxAge );
                }
                if (hostnameWebappHeader != null) {
                    // Not using CacheChannelHeader here to avoid creation of yet another object just for this.
                    scrutinizer.addHeader(CacheControl.CACHE_CONTROL, "group=\"" + hostnameWebappHeader + "\"");
                }
                chain.doFilter(request, scrutinizer);
            } else {
                chain.doFilter(request, response);
            }
            doTimingLogging(spath, "Finished treating filtered call", bench, System.currentTimeMillis());
        } finally {
            if (ndcInfo != null) {
                MDC.remove("app");
            }
        }
    }

    public void doTimingLogging(String path, String info, long startTime, long nowTime) {
        if (!doExtraLogging) {
            return;
        }
        if (path != null && !path.endsWith("apiadmin/ping")) {
            log.debug(path + ": ");
        }
    }

    /**
     * Used by unit tests
     * @return <code>true</code> if set, else <code>false</code>
     */
    protected boolean isHostNameWebappHeaderSet() {
        return hostnameWebappHeader != null;
    }

}
