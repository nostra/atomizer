package no.api.atomizer.mongodb.dao;

import com.mongodb.DB;
import com.wordnik.swagger.annotations.ApiModel;
import no.api.atomizer.core.MetaCounter;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
@ApiModel(value = "Counting an element")
@XmlRootElement(name = "Counter")
public final class MetaCounterMongoDao {
    private static final Logger log = LoggerFactory.getLogger(MetaCounterMongoDao.class);
    private final JacksonDBCollection<MetaCounter, String> collection;

    private MetaCounterMongoDao(JacksonDBCollection<MetaCounter, String> collection) {
        this.collection = collection;
    }

    public long size() {
        return collection.count();
    }


    /**
     * Factory method to create the dao
     */
    public static MetaCounterMongoDao createMetaCounterMongoDao(DB mongodb) {
        JacksonDBCollection<MetaCounter, String> staleGroupCollection =
                JacksonDBCollection.wrap(mongodb. createCollection("metacounter", null), MetaCounter.class, String.class);

        return new MetaCounterMongoDao(staleGroupCollection);
    }

    public void incrementCounterFor(String token) {
        collection.findAndModify(DBQuery.is("token", token), null, null, false, DBUpdate.inc("counter"), false, true);
    }

    /**
     * @return MetaCounter object matching token, or null
     */
    public MetaCounter findCounterFor(String token) {
        return collection.findOne(DBQuery.is("token", token));
    }

    public void deleteCounter(String token) {
        collection.remove(DBQuery.is("token", token));
    }

    public List<MetaCounter> listAllCounters() {
        List<MetaCounter> list = new ArrayList<>();
        DBCursor<MetaCounter> result = collection.find();
        while ( result.hasNext()) {
            list.add( result.next() );
        }
        Collections.sort(list, new MetaCounterNameComparator());
        return list;
    }

    private static class MetaCounterNameComparator implements Comparator<MetaCounter> {
        @Override
        public int compare(MetaCounter m1, MetaCounter m2) {
            return m1.getToken().compareToIgnoreCase(m2.getToken());
        }
    }
}

