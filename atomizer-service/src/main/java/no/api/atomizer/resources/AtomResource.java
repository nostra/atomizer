package no.api.atomizer.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import no.api.atomizer.core.StaleGroup;
import no.api.atomizer.exception.AtomizerPathException;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import no.api.atomizer.views.beans.GuiEntry;
import no.api.atomizer.views.beans.GuiFeed;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Produces(MediaType.APPLICATION_ATOM_XML)
@Api(value = "/event", description = "Atom XML feed resource. Supplies a window of events.")
@Path("/event")
public class AtomResource extends AbstractAtomizerResource {
    private static final String ARCHIVE_SLASH_PREFIX = "archive/";
    private final Logger log = LoggerFactory.getLogger(AtomResource.class);

    public AtomResource(StaleGroupMongoDao staleGroupMongoDao) {
        super(staleGroupMongoDao);
    }

    @GET @Path("/{idOrCurrent}")
    @ApiOperation(value = "Get Atom feed for current window, or from chosen ID",
            notes = "The parameter can either be \"current\" as static text, or an ID", response = Feed.class)
    @CacheControl(mustRevalidate = true)
    public Feed showFeed(@Context HttpServletRequest req, @PathParam("idOrCurrent") String idOrCurrent) {
        GuiFeed feed = createFeedWithDefaults(req);

        log.debug("* Incoming path for id or current value: "+idOrCurrent);
        if ( idOrCurrent.length() < 1 ) {
            throw new AtomizerPathException("Empty parameter not accepted. Try event/current");
        } else if ( idOrCurrent.startsWith("current") ) {
            fillFeedWithCurrent( feed );
        } else {
            if ( ! fillFeedWithSingleEvent( idOrCurrent, feed ) ) {
                throw new AtomizerPathException("path "+idOrCurrent+" not recognized as a selection of a single event.");
            }
        }

        return mapFeed( feed );
    }

    /**
     * Temporary conversion, should really just use the same presentation object everywhere...
     */
    private Feed mapFeed(GuiFeed gui) {
        Feed feed = new Abdera().newFeed();
        feed.setTitle(gui.getTitle());
        feed.setId(gui.getId() + "/current");
        feed.addLink(gui.getSelf(), "self");
        if ( gui.getPrevArchive() != null ) {
            feed.addLink(gui.getPrevArchive(), "prev-archive");
        }
        feed.setUpdated(gui.getUpdated());
        feed.addAuthor(gui.getAuthor());
        feed.addSimpleExtension("http://purl.org/syndication/cache-channel", "precision", "cc", "" + gui.getPrecision());
        feed.addSimpleExtension("http://purl.org/syndication/cache-channel", "lifetime", "cc", "" + gui.getLifetime());

        for ( GuiEntry guientry : gui.getEntries()) {
            Entry entry = feed.addEntry();
            entry.setId(gui.getId()+guientry.getId());
            entry.setTitle("stale");
            entry.setUpdated(guientry.getUpdated());
            for ( String link : guientry.getLinks()) {
                entry.addLink(link);
            }
            entry.addSimpleExtension("http://purl.org/syndication/cache-channel", "stale", "cc", null);
        }

        return feed;
    }

    @GET @Path("/archive/{timestamp}")
    @ApiOperation(value = "Get Atom feed for indicated window",
            notes = "The timestamp parameter indicate the start of the window for which to show feed entries", response = Feed.class)
    @CacheControl(mustRevalidate = true)
    public Feed showArchive(@Context HttpServletRequest req, @PathParam("timestamp") String timestamp) {
        GuiFeed feed = createFeedWithDefaults(req);
        fillFeedWithArchive(timestamp, feed);
        return mapFeed( feed );
    }


    /**
     *
     * @param id Should be a id to the event in question. If not, return false
     * @return false if no stale group found for path
     */
    private boolean fillFeedWithSingleEvent(String id, GuiFeed feed) {
        StaleGroup sg = getDao().findById(id);
        if ( sg != null ) {
            feed.setUpdated(new Date( sg.getUpdated()) );
            List<GuiEntry> entries = new ArrayList<>();
            entries.add( transformToGuiEntry( sg ));
            feed.setEntries(entries);
            Long fromUpdated = getDao().updatedLessThanUpdatedOf(Long.valueOf(sg.getUpdated()));
            if ( fromUpdated != null ) {
                feed.setPrevArchive(feed.getId()+ ARCHIVE_SLASH_PREFIX +fromUpdated);
            }

        } else {
            log.warn("No stale group entry found for id "+id);
            return false;
        }
        return true;
    }

    private void fillFeedWithArchive(String path, GuiFeed feed ) {
        List<StaleGroup> groups = getDao().retrieveStaleFromAndIncluding(Long.valueOf(path));
        log.debug("Got " + groups.size() + " stale groups");

        if (!groups.isEmpty()) {
            feed.setUpdated(new Date( groups.get(0).getUpdated()) );
            final StaleGroup last = groups.get(groups.size() -1 );

            feed.setPrevArchive(feed.getId()+ ARCHIVE_SLASH_PREFIX +(last.getUpdated() - 1 ));

            Long fromUpdated = getDao().updatedLessThanUpdatedOf(Long.valueOf(last.getUpdated()));
            if ( fromUpdated != null ) {
                feed.setPrevArchive(feed.getId()+ ARCHIVE_SLASH_PREFIX +fromUpdated);

            } else {
                log.debug("Did not find any elements older than "+last.getUpdated() );
            }
        }
        feed.setEntries(new ArrayList<GuiEntry>());
        for ( StaleGroup g : groups  ) {
            GuiEntry ge = transformToGuiEntry(g);
            feed.getEntries().add( ge );
        }
    }

}
