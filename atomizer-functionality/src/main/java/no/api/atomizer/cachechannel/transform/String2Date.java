package no.api.atomizer.cachechannel.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class String2Date {

    private static Logger log = LoggerFactory.getLogger(String2Date.class);

    private String2Date() {
        // Intentional
    }

    /**
     * Translating dates like "Mon, 02 Feb 2009 10:02:50 GMT" - which is
     * something that is useful when treating some request headers.
     * <b>However,</b> this method should <b>not</b> be used for treating other
     * dates. This is as the date is presumed to occur in the en_US locale.
     * <p> What we want is transform RFC 1123 date format into the long
     * representation of the date. See secion 14.21 in
     * <a href="ftp://ftp.rfc-editor.org/in-notes/rfc2616.txt">ftp://ftp.rfc-editor.org/in-notes/rfc2616.txt</a>.
     * </p>
     * @param fallback What to return when having error(s)
     */
    public static long transformStringDate( String toTransform, long fallback) {
        if ( toTransform == null ) {
            return fallback;
        }
        // May later need a more specified instance:
        Locale locale = new Locale("en", "US");
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", locale);

        try {
            Date date = sdf.parse(toTransform);
            return date.getTime();
        } catch (ParseException e) {
            log.debug("Exception is masked: " + e.getMessage());
            return fallback;
        }
    }

}
