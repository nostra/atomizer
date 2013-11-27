package no.api.atomizer.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
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
@Api(value = "/counter", description = "Counter CRUD operations. A counter would typically be used as part of css or js path reference in order to be able to force reload in browsers")
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
    @ApiOperation(value = "Find current value of indicated counter", notes = "If the counter is not found, the counter entry is zero.", response = MetaCounterMongoDao.class)
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
    @ApiOperation(value = "Adding counter, or updating value",
            notes = "Either make a new entry in the database, with a count of 1, or incrementing an existing count",
            response = MetaCounterMongoDao.class)
    public MetaCounter increment(@PathParam("name") String name) {
        counterDao.incrementCounterFor(name);
        return getCounter(name);
    }

    @DELETE
    @CacheControl(mustRevalidate = true)
    @Path("/{name}")
    @ApiOperation(value = "Remove the counter", notes = "Deleting the counter permanently", response = MetaCounterMongoDao.class)
    public MetaCounter delete(@PathParam("name") String name) {
        MetaCounter counter = counterDao.findCounterFor(name);
        counterDao.deleteCounter(name);
        return counter;
    }

}
