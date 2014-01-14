package no.api.atomizer.cachechannel.transform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Date2String {
    private Date2String() {
    }

    private final static String RFC1123="EEE, dd MMM yyyy HH:mm:ss zzz";
    /**
     * GMT timezone - all HTTP dates are on GMT
     */
    private final static TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");
    private final static SimpleDateFormat RFC1123_DATEFORMAT = new SimpleDateFormat(RFC1123, new Locale("en", "US"));

    static {
            RFC1123_DATEFORMAT.setTimeZone(GMT_ZONE);
    }

    /**
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21">RFC 2616</a> states in section 14.21 that
     * format is an absolute date and time as defined by HTTP-date in section
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1">3.3.1</a>;
     * it MUST be in RFC 1123.
     * 
     * @param date transform to RFC 1123 type of date.
     * @return String containing the formatted date, or null if parameter is null.
     * @see no.api.pantheon.transform.String2Date#transformStringDate(String, long) 
     */
    public static String transformDateToRfc1123String( Date date ) {
        if ( date == null ) {
            return null;
        }
        return ((DateFormat) RFC1123_DATEFORMAT.clone()).format(date);

    }

    /**
     * @deprecated Use #transformDateToRfc1123String instead of this method, as the method name is misleading
     */
    public static String transformDateToString( Date date ) {
        return transformDateToRfc1123String(date);        
    }
}
