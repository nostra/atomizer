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
        DBObject ping = new BasicDBObject("ping", "1");
        try {
            mongodb.command(ping);
        } catch (MongoException e) {
            return Result.unhealthy(e);
        }
        return Result.healthy();
    }
}
