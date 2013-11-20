package no.api.atomizer.views;

import com.yammer.dropwizard.views.View;
import no.api.atomizer.core.MetaCounter;
import no.api.atomizer.views.beans.GuiFeed;

import java.util.List;

/**
 *
 */
public class IndexView extends View {

    private final List<MetaCounter> metaCounters;

    private GuiFeed feed;

    public IndexView(GuiFeed feed, List<MetaCounter> metaCounters) {
        super("index.ftl");
        this.feed = feed;
        this.metaCounters = metaCounters;
    }

    public List<MetaCounter> getMetaCounters() {
        return metaCounters;
    }

    public GuiFeed getFeed() {
        return feed;
    }
}
