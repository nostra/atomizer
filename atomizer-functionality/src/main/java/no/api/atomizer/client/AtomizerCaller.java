package no.api.atomizer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This class will post JSON data (as created by strings) to the atomizer endpoint.
 * Data are not serialized in any way, but hard coded strings. This is as we want this
 * package to have as few dependencies as possible.
 */
public class AtomizerCaller {
    private static final Logger log = LoggerFactory.getLogger(AtomizerCaller.class);
    private final String atomizerEndpoint;

    /**
     * Retrieve the atomizer endpoint from wp.xml, gaia or somewhere, and insert it here.
     * @param atomizerEndpoint Something similar to http://localhost:9006/atomizer/
     */
    public AtomizerCaller(String atomizerEndpoint) {
        if ( atomizerEndpoint.endsWith("/")) {
            this.atomizerEndpoint = atomizerEndpoint;
        } else {
            this.atomizerEndpoint = atomizerEndpoint+"/";
        }
    }

    public static void main( String args[] ) throws URISyntaxException {
        AtomizerCaller caller = new AtomizerCaller("http://localhost:9006/atomizer/");
        caller.markStale("/somegroup");
        caller.incrementCounter("jawrcounter");
        log.debug("Reading counter: "+caller.readCounter("jawrcounter"));
    }

    public void incrementCounter(String counter) {
        log.debug("Incrementing "+counter);
        String result = postToAtomizer( "counter/"+counter, "", "PUT");
        log.debug("Return value from put: " + result);
    }

    /**
     * You will get JSON as return value. It is left to the caller to deserialize this.
     */
    public String readCounterAsJSON(String counter) {
        URI uri = URI.create(atomizerEndpoint+"counter/"+counter);
        String result = null;
        try {
            result = readLinesFromStream(uri.toURL().openStream());
        } catch (IOException e) {
            log.error("Got exception", e);
        }

        return result;
    }

    /**
     * @return Value of counter, negative value if no counter exists or error.
     */
    public int readCounterAsInteger(String counter) {
        String read = readCounterAsJSON(counter);
        if ( read != null ) {
            String[] split = counter.split(":");

            if ( split.length > 2  && split[2].contains(",")) {
                try {
                    return Integer.parseInt( split[2].substring(0, split[2].indexOf(",")) );
                } catch ( NumberFormatException nfe ) {
                    log.error("Did not expected NFE from "+counter, nfe);
                }
            }
        }
        log.debug("Could not find counter, or an error occurred. Counter: {}", counter);
        return -1;
    }

        /**
         * @deprecated Use #readCounterAsJSON instead
         */
    public String readCounter(String counter) {
        return readCounterAsJSON(counter);
    }

    public void markStale( String groupNameWithSlash ) {
        log.debug("To mark the following stale: "+groupNameWithSlash);
        String result = postToAtomizer( "stalegroup", "{\"path\":\""+groupNameWithSlash+"\"}", "POST");
        log.debug("Return value from post: "+result);
    }

    /**
     * Notice: Any exceptions are intentionally swallowed and only logged.
     */
    private String postToAtomizer(String target, String json, String method) {
        HttpURLConnection connection = null;
        String result = "";
        try {
            connection = (HttpURLConnection) new URL(atomizerEndpoint + target).openConnection();
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestMethod(method);

            try ( OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream())) {
                out.write(json);
                out.flush();
            }
            result = readLinesFromStream(connection.getInputStream());

        } catch (IOException e) {
            // It is intentional that we only log the exception
            log.error("Got exception", e);
        } finally {
            if ( connection != null ) {
                connection.disconnect();
            }
        }

        return result;
    }

    private String readLinesFromStream(InputStream inputstream) throws IOException {
        StringBuilder result = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream) ) ) {
            String line = reader.readLine();
            while ( line != null ) {
                if ( result.length() > 0 ) {
                    result.append("\n");
                }
                result.append(line);
                line = reader.readLine();
            }
        }
        return result.toString();
    }
}
