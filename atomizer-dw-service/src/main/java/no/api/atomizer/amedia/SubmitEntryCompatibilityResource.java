package no.api.atomizer.amedia;

import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.core.StaleGroup;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 */
@Path("/submitEntry.html")
@Produces(MediaType.TEXT_PLAIN)
public class SubmitEntryCompatibilityResource {
    private static final Logger log = LoggerFactory.getLogger(SubmitEntryCompatibilityResource.class);

    private StaleGroupMongoDao staleGroupMongoDao;

    public SubmitEntryCompatibilityResource(StaleGroupMongoDao staleGroupMongoDao) {
        this.staleGroupMongoDao = staleGroupMongoDao;
    }

    @POST
    @CacheControl(mustRevalidate = true)
    public String postSubmitEntry(@FormParam("path") String path) {
        log.debug("Got incoming path: "+path);
        StaleGroup sg = new StaleGroup();
        sg.setPath(path);
        staleGroupMongoDao.insert(sg);
        return "ok";
    }

    @GET
    @CacheControl(mustRevalidate = true)
    public String getSubmitEntry(@QueryParam("path") String path) {
        log.debug("Got incoming path: "+path);
        return postSubmitEntry(path);
    }
}