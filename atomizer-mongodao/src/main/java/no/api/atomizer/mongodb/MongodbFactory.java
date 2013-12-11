package no.api.atomizer.mongodb;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class MongodbFactory {
    private MongodbFactory() {
        // Intentional
    }

    public static DB createMongoDb(MongodbConfiguration mongodbConfiguration) throws UnknownHostException {
        return  new MongodbFactory().configure(mongodbConfiguration);
    }

    private DB configure(MongodbConfiguration configuration) throws UnknownHostException {
        List<ServerAddress> servers = new ArrayList<>();
        for ( String host : configuration.getHosts()) {
            ServerAddress sa = new ServerAddress(host, configuration.getPort());
            servers.add(sa);
        }
        MongoClient client = new MongoClient(servers);
        if ( configuration.getWriteConcern() != null ) {
            client.setWriteConcern(configuration.getWriteConcern());
        }
        return client.getDB(configuration.getName());
    }
}
