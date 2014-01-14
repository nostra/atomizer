package no.api.atomizer.resources;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.yammer.dropwizard.testing.ResourceTest;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import no.api.atomizer.mongodb.health.MongodbHealth;
import org.junit.Ignore;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assume.assumeTrue;

/**
 *
 */
public class IndexPageResourceTest extends ResourceTest {
    private StaleGroupMongoDao mongoDao;

    @Override
    protected void setUpResources() throws UnknownHostException {
        MongoClient mongo = new MongoClient("127.0.0.1", 27017);
        DB db = mongo.getDB("unittest");
        MongodbHealth mh = new MongodbHealth(db);
        assumeTrue(mh.isAlive());

        mongoDao = StaleGroupMongoDao.createStaleGroupMongoDao(db);
        addResource(new IndexPageResource(mongoDao, null));
    }

    @Test
    @Ignore
    public void testGetIndex() throws Exception {
        // Trouble calling method due to http servlet request parameter.
    }

}
