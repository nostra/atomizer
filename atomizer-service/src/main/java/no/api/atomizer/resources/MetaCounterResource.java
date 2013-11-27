package no.api.atomizer.resources;

import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.core.MetaCounter;
import no.api.atomizer.mongodb.dao.MetaCounterMongoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 */
@Path("/counter")
@Produces(MediaType.APPLICATION_JSON)
public class MetaCounterResource {
    private static final Logger log = LoggerFactory.getLogger(StaleGroupResource.class);

    private final MetaCounterMongoDao counterDao;

    public MetaCounterResource (MetaCounterMongoDao counterDao) {
        this.counterDao = counterDao;
    }

    // Swagger doc: http://localhost:9106/atomizer/api-docs/counter

    @GET
    @CacheControl(mustRevalidate = true)
    @Path("/{name}")
    public MetaCounter getCounter(@PathParam("name") String name) {
        MetaCounter counter = counterDao.findCounterFor(name);
        if ( counter == null ) {
            counter = new MetaCounter();
            counter.setCounter(Integer.valueOf(0));
            counter.setToken(name);
        }
        return counter;
    }

    @PUT
    @CacheControl(mustRevalidate = true)
    @Path("/{name}")
    public MetaCounter increment(@PathParam("name") String name) {
        counterDao.incrementCounterFor(name);
        return getCounter(name);
    }

    @DELETE
    @CacheControl(mustRevalidate = true)
    @Path("/{name}")
    public MetaCounter delete(@PathParam("name") String name) {
        MetaCounter counter = counterDao.findCounterFor(name);
        counterDao.deleteCounter(name);
        return counter;
    }

}
