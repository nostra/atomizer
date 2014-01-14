package no.api.atomizer.amedia;

import com.thoughtworks.xstream.XStream;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.core.MetaCounter;
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
    public Response incrementCounterFor(@FormParam("payload") String payload) {
        counterMongoDao.incrementCounterFor((String) xstream.fromXML(payload));
        return Response.ok().build();
    }

    @GET
    @CacheControl(mustRevalidate = true)
    @Path("/count/{token}.xstream")
    public Response counter( @PathParam("token") String token ) {
        MetaCounter counter = counterMongoDao.findCounterFor(token);
        if ( counter == null ) {
            log.info("Counter {} does not exist yet", token);
            counter = new MetaCounter();
            counter.setToken(token);
            counter.setCounter(Integer.valueOf(0));
        } else {
            log.info("Found counter {}", counter);
        }
        CounterHolder ch = new CounterHolder();
        ch.setToken(counter.getToken());
        ch.setCounter(counter.getCounter());
        ch.setId(Long.valueOf(666)); // Fake ID
        return Response.ok(xstream.toXML(ch)).build();
    }


    @POST
    @CacheControl(mustRevalidate = true)
    @Consumes("application/x-www-form-urlencoded")
    @Path("/insert.xstream")
    public Response insert(@FormParam("payload") String payload, @Context HttpServletRequest req) {
        log.info("Incoming: "+payload);
        StaleGroup sg = (StaleGroup) xstream.fromXML(payload);
        staleGroupMongoDao.insert(translateToNewStaleGroupType(sg));
        return Response.ok(xstream.toXML(sg), MediaType.APPLICATION_XML).build();
    }

    private no.api.atomizer.core.StaleGroup translateToNewStaleGroupType(StaleGroup sg) {
        no.api.atomizer.core.StaleGroup internal = new no.api.atomizer.core.StaleGroup();
        internal.setUpdated(sg.getUpdated());
        internal.setPath(sg.getPath());
        return internal;
    }
/*
/atomizer/rest/islocked/577907.xstream
/atomizer/submitEntry.html?path=%2Fsec86 HTTP/1.1" 302 0 "-" "ecerenovator/1.0"
/atomizer/pathlist.html HTTP/1.1" 200 5715 "-" "ecerenovator/1.0"
/atomizer/rest/insert.xstream
/atomizer/submitEntry.html?path=/zad2551940

/atomizer/pathlist.html
/atomizer/rest/count/jawrcounter.xstream
/atomizer/rest/islocked/101.xstream
/atomizer/submitEntry.html?path=%2Fart1931650
/atomizer/submitEntry.html?path=/zcm3028621
/atomizer/rest/insert.xstream
/atomizer/submitEntry.html

 */


}
