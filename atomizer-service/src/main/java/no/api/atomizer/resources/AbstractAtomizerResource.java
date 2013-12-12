package no.api.atomizer.resources;

import no.api.atomizer.core.StaleGroup;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import no.api.atomizer.views.beans.GuiEntry;
import no.api.atomizer.views.beans.GuiFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public abstract class AbstractAtomizerResource {
    private static final Logger log = LoggerFactory.getLogger(AbstractAtomizerResource.class);

    private final StaleGroupMongoDao dao;

    public AbstractAtomizerResource(StaleGroupMongoDao staleGroupMongoDao) {
        this.dao = staleGroupMongoDao;
    }

    public StaleGroupMongoDao getDao() {
        return dao;
    }

    protected GuiEntry transformToGuiEntry(StaleGroup g) {
        GuiEntry ge = new GuiEntry();
        ge.setId("" + g.getId());
        ge.setUpdated(new Date(g.getUpdated()));
        List<String> links = new ArrayList<>();
        links.add(g.getPath());
        ge.setLinks(links);
        return ge;
    }

    protected void fillFeedWithCurrent(GuiFeed feed) {
        List<StaleGroup> groups = dao.retrieveCurrentStale();
        log.debug("Got " + groups.size() + " stale groups");

        if (!groups.isEmpty()) {
            feed.setUpdated(new Date(groups.get(0).getUpdated()));
            Long fromUpdated = dao.updatedLessThanUpdatedOf(Long.valueOf(groups.get(groups.size() - 1).getUpdated()));
            if (fromUpdated != null) {
                feed.setPrevArchive(feed.getId() + "archive/" + fromUpdated);
            }


        }
        feed.setEntries(new ArrayList<GuiEntry>());
        for (StaleGroup g : groups) {
            GuiEntry ge = transformToGuiEntry(g);
            feed.getEntries().add(ge);
        }
    }




    protected GuiFeed createFeedWithDefaults(HttpServletRequest req) {
        GuiFeed feed = new GuiFeed();
        int lifetimeSeconds = dao.resolveLifetimeSeconds();
        feed.setLifetime(lifetimeSeconds);
        feed.setPrecision(StaleGroupMongoDao.RESOLUTION_SECONDS);
        feed.setSelf(req.getRequestURL().toString());
        feed.setUpdated(new Date());
        /*
               log.info("--------------- *-PathTranslated: "+req.getPathTranslated()+
                "\nContextPath: "+req.getContextPath()+
                "\nPathInfo: "+req.getPathInfo()+
                "\nreqURI: "+req.getRequestURI()+
                "\nServletPath(): "+req.getServletPath()+
                "\nRequestURL(): "+req.getRequestURL()
        );
        */
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = req.getServletPath();
        }
        // Id shall typically be http://somewhere:port/atomizer/event/
        feed.setId(feed.getSelf().substring(0, (feed.getSelf().indexOf(pathInfo)) + 1)+"event/");
        feed.setTitle("Atomizer");
        feed.setEntries(new ArrayList<GuiEntry>());
        String host = null;
        try {
            host = new URL(req.getRequestURL().toString()).getHost();
        } catch (MalformedURLException ex) {
            log.error("Could not extract hostname from the requestURL. Defaulting to localhost. Masked exception: " + ex);
            host = "localhost";
        }
        feed.setAuthor(host);
        return feed;
    }


}
