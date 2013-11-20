package no.api.atomizer.mongodb.dao;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import no.api.atomizer.core.StaleGroup;
import no.api.atomizer.mongodb.health.MongodbHealth;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 *
 */
public class StaleGroupMongoDaoTest {
    private static final Logger log = LoggerFactory.getLogger(StaleGroupMongoDaoTest.class);

    private StaleGroupMongoDao staleDao;

    @Before
    public void setUp() throws UnknownHostException {
        MongoClient mongo = new MongoClient("127.0.0.1", 27017);
        DB db = mongo.getDB("unittest");
        MongodbHealth mh = new MongodbHealth(db);
        assumeTrue(mh.isAlive());

        staleDao = StaleGroupMongoDao.createStaleGroupMongoDao(db);
    }

    @Test
    public void testThatTableExists() {
        assertTrue(staleDao.size() >= 0);
        assertTrue(staleDao.resolveLifetimeSeconds() >= StaleGroupMongoDao.RESOLUTION_SECONDS);
    }

    @Test
    public void testStore() {
        long oldSize = staleDao.size();
        StaleGroup sg = new StaleGroup();
        sg.setPath("/junit");
        sg.setUpdated(System.currentTimeMillis());
        String id = staleDao.store(sg);
        assertEquals(oldSize + 1, staleDao.size());
        StaleGroup reread = staleDao.findById(id);
        assertEquals(reread.getPath(), sg.getPath());
        assertEquals(reread.getUpdated(), sg.getUpdated());
        staleDao.deleteById(id);
        assertEquals(oldSize, staleDao.size());
    }

    @Test
    public void testInsertAndDelete() {
        long size = staleDao.size();
        int currentSize = staleDao.retrieveCurrentStale().size();

        StaleGroup sg = new StaleGroup();
        sg.setPath("/path/15");
        sg.setUpdated(new Date().getTime());
        sg = staleDao.insert(sg);
        assertEquals(size + 1, staleDao.size());

        assertNotNull(staleDao.findById(sg.getId()));

        assertEquals("Expecting size increase of 1", currentSize + 1, staleDao.retrieveCurrentStale().size());
        assertNotNull(staleDao.findStaleGroupByPath(sg.getPath()));

        staleDao.deleteById(sg.getId());
        assertEquals(size, staleDao.size());
    }

    @Test
    public void testInsertAndRetrieval() {
        final int numOfTest = 10;
        long now = new Date().getTime();
        LinkedList<StaleGroup> ins = new LinkedList<StaleGroup>();
        for (int i = 0; i < numOfTest; i++) {
            StaleGroup sg = new StaleGroup();
            sg.setPath("/junit/" + i);
            sg.setUpdated(now + i);
            ins.addFirst(staleDao.insert(sg));
        }

        List<StaleGroup> stales = staleDao.retrieveCurrentStale();
        log.debug("StaleGroup elements inserted: \n" + ins + "\nPresent\n" + stales);
        for (int i = 0; i < numOfTest; i++) {
            StaleGroup stale = stales.get(i);
            assertEquals(ins.get(i).getId(), stale.getId());
        }

        for (StaleGroup stale : stales) {
            staleDao.deleteById(stale.getId());
        }
    }

    @Test
    public void testDeletionOfDoubleGroupWithDifferentTime() {
        final String path = "/path/15";
        Collection<StaleGroup> coll = staleDao.findStaleGroupByPath(path);
        assertEquals("Collection presumed to be empty as an invariant for the test", 0, coll.size());
        StaleGroup sg = new StaleGroup();
        sg.setPath("/path/15");
        sg.setUpdated(new Date().getTime() - 1);
        sg = staleDao.insert(sg);

        coll = staleDao.findStaleGroupByPath(path);
        assertEquals("Addition of an element is registered.", 1, coll.size());

        sg = new StaleGroup();
        sg.setPath("/path/15");
        sg.setUpdated(new Date().getTime());
        sg = staleDao.insert(sg);

        assertNotNull(sg);
        coll = staleDao.findStaleGroupByPath(path);
        assertEquals("Addition of one more element does not increase size of of table.", 1, coll.size());
        // TODO WIP
        coll = staleDao.findStaleGroupByPath(path.substring(0, 3)+".*");
        Assert.assertTrue("Want to be able to find item by sub path", coll.size() > 0);

        staleDao.deleteById(sg.getId());
        coll = staleDao.findStaleGroupByPath(path);
        assertEquals(0, coll.size());
    }

    @Test
    public void testThatDoubleInsertionIsOnlyInsertedOnce() {
        final String path = "/path/15";
        Collection<StaleGroup> coll = staleDao.findStaleGroupByPath(path);
        assertEquals("Collection presumed to be empty as an invariant for the test", 0, coll.size());

        StaleGroup sg = new StaleGroup();
        sg.setPath("/path/15");
        sg.setUpdated(new Date().getTime());
        sg = staleDao.insert(sg);
        // Resetting id is the same as a new entry
        sg.setId(null);
        sg = staleDao.insert(sg);

        coll = staleDao.findStaleGroupByPath(path);
        assertEquals("Two additions of the same element will only register once.", 1, coll.size());

        staleDao.deleteById(sg.getId());
        coll = staleDao.findStaleGroupByPath(path);
        assertEquals(0, coll.size());
    }

    @Test
    public void testUpdatedLessThanUpdatedOf() {
        assertNull(staleDao.updatedLessThanUpdatedOf(1L));
        StaleGroup sg = new StaleGroup();
        sg.setPath("/path/16");
        sg.setUpdated(new Date().getTime());
        sg = staleDao.insert(sg);
        assertNotNull(staleDao.findById(sg.getId()));
        Long update = new Date().getTime();
        assertEquals(sg.getUpdated(),
                staleDao.updatedLessThanUpdatedOf(update).longValue());
        assertTrue(staleDao.deleteById(sg.getId()));
    }

    @Test
    public void testRetrieveStaleFromAndIncluding() {
        assertEquals(0, staleDao.retrieveStaleFromAndIncluding(new Date().getTime()).size());

        List<StaleGroup> sgList = new ArrayList<>();
        StaleGroup sg = new StaleGroup();
        sg.setPath("/path/17");
        sg.setUpdated(new Date().getTime());
        sg = staleDao.insert(sg);
        sgList.add(sg);
        // Ensuring "next page"
        assertEquals(sgList.size(),
                staleDao.retrieveStaleFromAndIncluding(sg.getUpdated() + (2 * StaleGroupMongoDao.RESOLUTION_SECONDS))
                        .size());
        assertEquals(sgList.get(0).getId(),
                staleDao.retrieveStaleFromAndIncluding(new Date().getTime()).get(0).getId());
        assertTrue(staleDao.deleteById(sg.getId()));
    }

}
