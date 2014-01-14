package no.api.atomizer.mongodb.health;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.yammer.metrics.core.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MongodbHealth extends HealthCheck {
    private static final Logger log = LoggerFactory.getLogger(MongodbHealth.class);

    private final DB mongodb;

    public MongodbHealth(DB mongodb) {
        super("mongodb");
        this.mongodb = mongodb;
    }

    /**
     * For junit tests only
     */
    public boolean isAlive() {
        return check().isHealthy();
    }

    @Override
    protected Result check(){
        log.info("Checking mongodb");
        DBObject ping = new BasicDBObject("ping", "1");
        try {
            mongodb.command(ping);
        } catch (MongoException e) {
            log.info("Failed! Returning unhealthy", e);
            return Result.unhealthy(e);
        }
        log.info("Success! Returning health");
        return Result.healthy();
    }
}
