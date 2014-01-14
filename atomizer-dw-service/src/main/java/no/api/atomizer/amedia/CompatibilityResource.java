package no.api.atomizer.amedia;

import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.mongodb.dao.MetaCounterMongoDao;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
@Produces(MediaType.APPLICATION_XML)
@Path("/rest")
public class CompatibilityResource {
    private static final Logger log = LoggerFactory.getLogger(CompatibilityResource.class);

    private final StaleGroupMongoDao staleGroupMongoDao;

    private final MetaCounterMongoDao counterMongoDao;

    public CompatibilityResource(StaleGroupMongoDao staleGroupMongoDao, MetaCounterMongoDao counterMongoDao) {
        this.staleGroupMongoDao = staleGroupMongoDao;
        this.counterMongoDao = counterMongoDao;
    }

    @POST
    @CacheControl(mustRevalidate = true)
    @Consumes("application/x-www-form-urlencoded")
    @Path("/incrementcounter.xstream")
    public Response incrementCounterFor(@FormParam("payload") String payload, @Context HttpServletRequest req) {
        counterMongoDao.incrementCounterFor(xstream.fromXML(payload));
        return Response.ok().build();
    }

    public Response getIndex(@FormParam("payload") String payload, @Context HttpServletRequest req) {
        log.info("Incoming: "+payload);
        return Response.ok("<x>whee</x>", MediaType.APPLICATION_XML).build();
    }

}
