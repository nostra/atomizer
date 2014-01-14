package no.api.atomizer.mongodb.dao;

import com.mongodb.DB;
import com.mongodb.WriteConcern;
import no.api.atomizer.core.StaleGroup;
import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 */
public final class StaleGroupMongoDao {
    private static final Logger log = LoggerFactory.getLogger(StaleGroupMongoDao.class);

    /**
     * Resolution is the span of time covered in a particular report
     * TODO Consider to make the variable configurable
     */
    public static final int RESOLUTION_SECONDS = 30;

    /**
     * How long the information is valid. i.e. how long is the information valid
     */
    public static final int LIFETIME_SECONDS = 2592000;

    /**
     * Accepted lag in ms. Lag is when an entry is not updated if it already exists.
     */
    public static final long ACCEPTED_LAG_MS = 1500;

    private final JacksonDBCollection<StaleGroup, String> collection;

    private StaleGroupMongoDao(JacksonDBCollection<StaleGroup, String> collection) {
        this.collection = collection;
        this.collection.ensureIndex("updated");
    }

    public long size() {
        return collection.count();
    }

    /**
     * @return The resulting object, re-read from the database. If you don't
     * need the extra lookup, use {@link #store(no.api.atomizer.core.StaleGroup)}
     */
    public StaleGroup insert(StaleGroup sg) {
        return findById(store(sg));
    }

    /**
     * @return The ID from the database
     */
    public String store(StaleGroup stale) {
        if ( stale.getUpdated() == 0 ) {
            // Haven't got updated time as part of the parameter.
            stale.setUpdated(System.currentTimeMillis());
        }

        List<StaleGroup> old = collection.find(
                DBQuery.is("path", stale.getPath())
                        .lessThanEquals("updated", stale.getUpdated()))
                .sort(DBSort.desc("updated")).limit(1).toArray();
        if ( old.size() > 0 ) {
            StaleGroup toUpdate = old.get(0);
            if (toUpdate.getUpdated() > stale.getUpdated() - ACCEPTED_LAG_MS) {
                log.debug("Element exists within an acceptable lag time (which is "+ACCEPTED_LAG_MS+" ms). Therefore not updating it.");
                return toUpdate.getId();
            }

            log.debug(
                    "Already got at least one already registered entry within resolution. Updating that entry with new updated time.");
            toUpdate.setUpdated(stale.getUpdated());
            collection.updateById(toUpdate.getId(), toUpdate);
            return toUpdate.getId();
        }

        // Delete elements that are older than lifetime - here updated represents "now"
        collection.remove(DBQuery.lessThan("updated", stale.getUpdated() - (LIFETIME_SECONDS * 1000L)), WriteConcern.ERRORS_IGNORED);

        WriteResult<StaleGroup, String> wr = collection.insert(stale);
        return wr.getSavedId();
    }

    public StaleGroup findById( String id ) {
        return collection.findOneById(id);
    }

    public boolean deleteById( String id ) {
        WriteResult<StaleGroup, String> result = collection.removeById(id);
        return result.getLastError().ok();
    }

    /**
     * @return Current view of stale elements within default resolution range
     */
    public List<StaleGroup> retrieveCurrentStale() {
        List<StaleGroup> newestList = collection.find().sort(DBSort.desc("updated")).limit(1).toArray();
        if (!newestList.isEmpty()) {
            return retrieveStaleFromAndIncluding(newestList.get(0).getUpdated());
        }
        // Which at this point is empty anyway:
        return newestList;
    }

    /**
     * @param highMark is what the updated field in the stale group shall be less than or equal to.
     */
    public List<StaleGroup> retrieveStaleFromAndIncluding(Long highMark) {
        Long lowMark = Long.valueOf(Math.max(0, highMark.longValue() - (RESOLUTION_SECONDS * 1000L)));

        log.debug("Shall find elements between {} and {}", lowMark, highMark);
        List<StaleGroup> result = collection.find(DBQuery
                        .greaterThanEquals("updated", lowMark)
                        .lessThanEquals("updated", highMark))
                        .sort(DBSort.desc("updated"))
                        .toArray();
        if (result.isEmpty()) {
            // Spool to next probable element.
            Long oneBelow = updatedLessThanUpdatedOf(highMark);
            if (oneBelow != null && oneBelow.longValue() > 0L ) {
                log.debug("Recursing in order to find next element (group). High mark adjusted to " + oneBelow + ". " +
                        "This is as the data has a gap (i.e. " +
                        "within precision, we have no data) in them.");
                return retrieveStaleFromAndIncluding(oneBelow);
            }
        }
        return result;
    }

    public Collection<StaleGroup> findStaleGroupByPath(String path) {
        Pattern pattern = Pattern.compile(path);
        List<StaleGroup> result = collection.find(DBQuery.regex("path", pattern))
                .sort(DBSort.desc("updated")).toArray();
        return result;
    }

    /**
     * @return The largest updated value which is lower than the one sent as parameter, or null if none
     */
    public Long updatedLessThanUpdatedOf(Long updtToBeLessThan) {
        List<StaleGroup> result = collection.find(DBQuery
                .lessThan("updated", updtToBeLessThan))
                .sort(DBSort.desc("updated"))
                .limit(1).toArray();
        if ( !result.isEmpty()) {
            return Long.valueOf(result.get(0).getUpdated());
        }
        return null;
    }

    /**
     * Resolution seconds is calculated based on the lowest value in the database.
     * It will not exceed the max lifetime value, and will be larger than the
     * resolution. Just used to get sensible defaults.
     */
    public int resolveLifetimeSeconds() {
        List<StaleGroup> low = collection.find().sort(DBSort.asc("updated")).limit(1).toArray();
        long lowMarkSec = 0;
        if ( low.size() > 0 ) {
            lowMarkSec = System.currentTimeMillis() - low.get(0).getUpdated();
            lowMarkSec = lowMarkSec / 1000;
        }
        // Not allowing more than lifetime secs as value
        lowMarkSec = Math.min(LIFETIME_SECONDS, lowMarkSec);
        // Logically, it should be larger than resolution seconds
        lowMarkSec = Math.max(RESOLUTION_SECONDS, lowMarkSec);

        return (int) lowMarkSec;
    }

    /**
     * Factory method to create the dao
     */
    public static StaleGroupMongoDao createStaleGroupMongoDao(DB mongodb) {
        JacksonDBCollection<StaleGroup, String> staleGroupCollection =
                JacksonDBCollection.wrap(mongodb. createCollection("stalegroup", null), StaleGroup.class, String.class);

        return new StaleGroupMongoDao(staleGroupCollection);
    }
}
