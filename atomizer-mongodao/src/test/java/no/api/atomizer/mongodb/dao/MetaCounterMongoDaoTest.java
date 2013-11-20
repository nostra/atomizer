package no.api.atomizer.mongodb.dao;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import no.api.atomizer.core.MetaCounter;
import no.api.atomizer.mongodb.health.MongodbHealth;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 *
 */
public class MetaCounterMongoDaoTest {
    private static final Logger log = LoggerFactory.getLogger(MetaCounterMongoDaoTest.class);

    private MetaCounterMongoDao counterDao;

    @Before
    public void setUp() throws UnknownHostException {
        MongoClient mongo = new MongoClient("127.0.0.1", 27017);
        DB db = mongo.getDB("unittest");
        MongodbHealth mh = new MongodbHealth(db);
        assumeTrue(mh.isAlive());

        counterDao = MetaCounterMongoDao.createMetaCounterMongoDao(db);
    }

    @Test
    public void testSimpleCounterBehaviour() {
        long oldSize = counterDao.size();
        MetaCounter c = counterDao.findCounterFor("junit");
        assertNull("Assuming this counter would not exist in beforehand", c);
        counterDao.incrementCounterFor("junit");
        assertEquals(1, counterDao.findCounterFor("junit").getCounter().intValue());
        assertEquals(oldSize + 1, counterDao.size());
        counterDao.incrementCounterFor("junit1");
        assertEquals(oldSize + 2, counterDao.size());
        counterDao.deleteCounter("junit");
        counterDao.deleteCounter("junit1");
        assertEquals(oldSize, counterDao.size());
    }

    @Test
    public void testSorting() {
        counterDao.incrementCounterFor("sort-b");
        counterDao.incrementCounterFor("sort-c");
        counterDao.incrementCounterFor("sort-a");
        List<MetaCounter> list = counterDao.listAllCounters();
        int indexForA = -1;
        int i = 0;
        while ( indexForA == -1 && i < list.size() ) {
            if ( list.get(i).getToken().startsWith("sort-")) {
                indexForA = i;
            }
        }
        assertTrue(indexForA != -1);
        assertEquals("sort-a", list.get(indexForA).getToken());
        assertEquals("sort-b", list.get(indexForA+1).getToken());
        assertEquals("sort-c", list.get(indexForA+2).getToken());
        counterDao.deleteCounter("sort-a");
        counterDao.deleteCounter("sort-b");
        counterDao.deleteCounter("sort-c");
    }
}