package no.api.atomizer.amedia;

import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.resources.IndexPageResource;
import no.api.pantheon.io.PantheonFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A different kind of resource than the v3 ping
 */
@Path("/apiadmin/ping")
@Produces(MediaType.TEXT_PLAIN)
public class AtomizerPingResource {

    private static final Logger log = LoggerFactory.getLogger(IndexPageResource.class);

    private final int adminPort;


    public AtomizerPingResource(int adminPort) {
        this.adminPort = adminPort;
    }

    @GET
    @CacheControl(mustRevalidate = true)
    public String doPing() {
        String result;
        try {
            URL local = new URL("http://localhost:"+adminPort+"/healthcheck");
            HttpURLConnection conn = (HttpURLConnection) local.openConnection();
            conn.setReadTimeout(5000);
            int responseCode = conn.getResponseCode();
            if ( responseCode == HttpURLConnection.HTTP_OK ) {
                return "OK";
            }
            result = PantheonFileReader.createInstance().inputStreamToForcedUTF8String(local.openStream());

        } catch ( IOException e ) {
            log.error("Got exception.", e);
            result = "Got some kind of trouble "+e;
        }

        // TODO Change later to Atomizer exception
        throw new RuntimeException(""+result);
    }
}
