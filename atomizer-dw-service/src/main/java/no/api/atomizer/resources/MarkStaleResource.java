package no.api.atomizer.resources;

import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.core.StaleGroup;
import no.api.atomizer.mongodb.dao.MetaCounterMongoDao;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import no.api.atomizer.views.IndexView;
import no.api.atomizer.views.beans.GuiFeed;
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

/**
 *
 */
@Path("/submit.html")
@Produces(MediaType.TEXT_HTML)
public class MarkStaleResource extends AbstractAtomizerResource {
    private static final Logger log = LoggerFactory.getLogger(MarkStaleResource.class);

    private final StaleGroupMongoDao dao;

    private final MetaCounterMongoDao counterMongoDao;

    public MarkStaleResource(StaleGroupMongoDao staleGroupMongoDao, MetaCounterMongoDao counterMongoDao) {
        super(staleGroupMongoDao);
        this.dao = staleGroupMongoDao;
        this.counterMongoDao = counterMongoDao;
    }

    @POST
    @CacheControl(mustRevalidate = true)
    @Consumes("application/x-www-form-urlencoded")
    public IndexView getIndex(@FormParam("path") String path, @Context HttpServletRequest req) {
        StaleGroup sg = new StaleGroup();
        sg.setUpdated(System.currentTimeMillis());
        sg.setPath(path);
        dao.store(sg);

        GuiFeed feed = createFeedWithDefaults(req);
        fillFeedWithCurrent(feed);
        return new IndexView(feed, counterMongoDao.listAllCounters());
    }
}
