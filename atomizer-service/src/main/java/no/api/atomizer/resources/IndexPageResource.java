package no.api.atomizer.resources;

import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.mongodb.dao.MetaCounterMongoDao;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import no.api.atomizer.views.IndexView;
import no.api.atomizer.views.beans.GuiFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 *
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class IndexPageResource extends AbstractAtomizerResource {
    private static final Logger log = LoggerFactory.getLogger(IndexPageResource.class);

    private final MetaCounterMongoDao counterMongoDao;

    public IndexPageResource(StaleGroupMongoDao staleGroupMongoDao, MetaCounterMongoDao counterMongoDao) {
        super(staleGroupMongoDao);
        this.counterMongoDao = counterMongoDao;
    }

    @GET
    @CacheControl(mustRevalidate = true)
    public IndexView getIndex(@Context HttpServletRequest req) {
        GuiFeed feed = createFeedWithDefaults(req);
        fillFeedWithCurrent(feed);

        return new IndexView(feed, counterMongoDao.listAllCounters());
    }
}
