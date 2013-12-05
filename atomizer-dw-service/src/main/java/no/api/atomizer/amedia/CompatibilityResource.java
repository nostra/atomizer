package no.api.atomizer.amedia;

import com.thoughtworks.xstream.XStream;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.mongodb.dao.MetaCounterMongoDao;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import no.api.atomizer.transport.CounterHolder;
import no.api.atomizer.transport.StaleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

    private XStream xstream = new XStream();

    public CompatibilityResource(StaleGroupMongoDao staleGroupMongoDao, MetaCounterMongoDao counterMongoDao) {
        this.staleGroupMongoDao = staleGroupMongoDao;
        this.counterMongoDao = counterMongoDao;
    }

    @POST
    @CacheControl(mustRevalidate = true)
    @Consumes("application/x-www-form-urlencoded")
    @Path("/incrementcounter.xstream")
    public Response incrementCounterFor(@FormParam("payload") String payload, @Context HttpServletRequest req) {
        log.info("Incoming: "+payload);
        // TODO perform work
        return Response.ok().build();
    }

    @GET
    @CacheControl(mustRevalidate = true)
    @Path("/count/${token}.xstream")
    public Response counter( @PathParam("token") String token ) {
        CounterHolder ch = new CounterHolder();
        ch.setToken(token);
        ch.setCounter(666);
        ch.setId(Long.valueOf(666));
        // TODO return real data...
        return Response.ok(xstream.toXML(ch)).build();
    }


    @POST
    @CacheControl(mustRevalidate = true)
    @Consumes("application/x-www-form-urlencoded")
    @Path("/insert.xstream")
    public Response insert(@FormParam("payload") String payload, @Context HttpServletRequest req) {
        log.info("Incoming: "+payload);
        StaleGroup sg = (StaleGroup) xstream.fromXML(payload);
        // TODO return real data...
        return Response.ok(xstream.toXML(sg), MediaType.APPLICATION_XML).build();
    }



}
