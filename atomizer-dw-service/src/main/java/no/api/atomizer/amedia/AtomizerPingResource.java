package no.api.atomizer.amedia;

import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.resources.IndexPageResource;
import no.api.pantheon.io.PantheonFileReader;
import no.api.pantheon.maven.VersionFileExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static javax.ws.rs.core.Response.Status;

/**
 * A different kind of resource than the v3 ping
 */
@Path("/apiadmin/ping")
@Produces(MediaType.TEXT_PLAIN)
public class AtomizerPingResource {

    private static final Logger log = LoggerFactory.getLogger(IndexPageResource.class);

    private final int adminPort;

    private final String version;


    public AtomizerPingResource(int adminPort) {
        this.adminPort = adminPort;
        this.version = VersionFileExtractor.extractor().whatIsGitVersion();
    }

    @GET
    @CacheControl(mustRevalidate = true)
    public Response doPing() {
        String result;
        try {
            URL local = new URL("http://localhost:"+adminPort+"/healthcheck");
            HttpURLConnection conn = (HttpURLConnection) local.openConnection();
            conn.setReadTimeout(5000);
            int responseCode = conn.getResponseCode();
            if ( responseCode == HttpURLConnection.HTTP_OK ) {
                return Response.ok("ok "+version, MediaType.TEXT_PLAIN_TYPE).status(new OkStatus("ok "+version)).build();
            }
            result = PantheonFileReader.createInstance().inputStreamToForcedUTF8String(conn.getErrorStream());

        } catch ( IOException e ) {
            log.error("Got exception.", e);
            result = "Got some kind of trouble "+e;
        }

        return Response.ok(result, MediaType.TEXT_PLAIN_TYPE).status(500).build();
    }

    /**
     * Not a big success. Seems like this status gets ignored after all.
     */
    private class OkStatus implements Response.StatusType {

        private String phrase;

        public OkStatus(String phrase) {
            this.phrase = phrase;
        }

        @Override
        public int getStatusCode() {
            return HttpURLConnection.HTTP_OK ;
        }

        @Override
        public Status.Family getFamily() {
            return Status.Family.SUCCESSFUL;
        }

        @Override
        public String getReasonPhrase() {
            return phrase;
        }
    }
}
