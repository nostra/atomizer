package no.api.atomizer.resources;

import com.sun.jersey.api.NotFoundException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.core.StaleGroup;
import no.api.atomizer.exception.AtomizerUnexpectedException;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 *
 */
@Path("/stalegroup")
@Api(value = "/stalegroup", description = "Stale group CRUD operations. This is what the group part of your cache-channel header will contain")
@Produces(MediaType.APPLICATION_JSON)
public class StaleGroupResource {
    private static final Logger log = LoggerFactory.getLogger(StaleGroupResource.class);

    private final StaleGroupMongoDao sgdao;

    public StaleGroupResource(StaleGroupMongoDao sgdao) {
        this.sgdao = sgdao;
    }

    @GET
    @CacheControl(mustRevalidate = true)
    @Path("/{id}")
    @ApiOperation(value = "Find a single stale group by ID", notes = "Method is here mainly for completeness", response = StaleGroup.class)
    public StaleGroup getStaleGroup(@PathParam("id") String id) {
        final StaleGroup stalegroup = sgdao.findById(id);
        if ( stalegroup == null ) {
            throw new NotFoundException("No such stalegroup.");
        }
        return stalegroup;
    }


    @DELETE
    @Path("/{id}")
    @CacheControl(mustRevalidate = true)
    @ApiOperation(value = "Permanently delete a single stale group by ID",
            notes = "Method is here mainly for completeness. Atomizer will clean up by itself.",
            response = StaleGroup.class)
    public StaleGroup deleteById(@PathParam("id") String id) {
        final StaleGroup stalegroup = sgdao.findById(id);
        if ( stalegroup == null ) {
            throw new NotFoundException("No such stalegroup.");
        }
        if ( !sgdao.deleteById(id)) {
            throw new AtomizerUnexpectedException("Unable to delete element with id "+id);
        }
        return stalegroup;
    }


    @POST
    @CacheControl(mustRevalidate = true)
    @ApiOperation(value = "Insert or update a stalegroup", notes = "This would be the most used method. " +
            "It inserts a stalegroup item into the database to be displayed in the current window",
            response = Response .class) @Consumes({MediaType.APPLICATION_JSON})
    public Response insert( @Valid StaleGroup staleGroup ) {
        String id = sgdao.store(staleGroup);
        log.info("Inserted element with id: "+id);
        if ( id != null ) {
            return Response.created( UriBuilder.fromResource(StaleGroupResource.class)
                    .build(id))
                    .build();
        }
        return Response.serverError().build();
    }

}
