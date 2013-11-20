package no.api.atomizer;

import com.mongodb.DB;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;
import no.api.atomizer.mongodb.MongodbFactory;
import no.api.atomizer.mongodb.dao.MetaCounterMongoDao;
import no.api.atomizer.mongodb.dao.StaleGroupMongoDao;
import no.api.atomizer.mongodb.health.MongodbHealth;
import no.api.atomizer.resources.AtomResource;
import no.api.atomizer.resources.IndexPageResource;
import no.api.atomizer.resources.MarkStaleResource;
import no.api.atomizer.resources.MetaCounterResource;
import no.api.atomizer.resources.SearchForGroupResource;
import no.api.atomizer.resources.StaleGroupResource;

import java.net.UnknownHostException;

/**
 *
 */
public class AtomizerService extends Service<AtomizerConfiguration> {
    public static void main(String[] args) throws Exception { // NOSONAR Nevermind...
        new AtomizerService().run(args);
    }

    @Override
    public void initialize(Bootstrap<AtomizerConfiguration> bootstrap) {
        bootstrap.setName("atomizer");
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle("/assets/js", "/atomizer/js"));
        bootstrap.addBundle(new AssetsBundle("/assets/css", "/atomizer/css"));
        bootstrap.addBundle(new AssetsBundle("/assets/images", "/atomizer/images"));
    }

    @Override
    public void run(AtomizerConfiguration config, Environment environment)
            throws ClassNotFoundException, UnknownHostException {
        DB mongodb = MongodbFactory.createMongoDb(config.getMongoConfiguration());
        environment.addHealthCheck(new MongodbHealth(mongodb));
        StaleGroupMongoDao staleGroupMongoDao = StaleGroupMongoDao.createStaleGroupMongoDao(mongodb);
        MetaCounterMongoDao counterMongoDao = MetaCounterMongoDao.createMetaCounterMongoDao(mongodb);

        environment.addResource(new IndexPageResource(staleGroupMongoDao, counterMongoDao));
        environment.addResource(new StaleGroupResource(staleGroupMongoDao));
        environment.addResource(new MarkStaleResource(staleGroupMongoDao, counterMongoDao));
        environment.addResource(new SearchForGroupResource(staleGroupMongoDao, counterMongoDao));
        environment.addResource(new AtomResource(staleGroupMongoDao));
        environment.addResource(new MetaCounterResource(counterMongoDao));

        addSwaggger( environment );
    }

    private void addSwaggger(Environment environment) {
        // Swagger Resource
        environment.addResource(new ApiListingResourceJSON());

        // Swagger providers
        environment.addProvider(new ApiDeclarationProvider());
        environment.addProvider(new ResourceListingProvider());

        // Swagger Scanner, which finds all the resources for @Api Annotations
        ScannerFactory.setScanner(new DefaultJaxrsScanner());

        // Add the reader, which scans the resources and extracts the resource information
        ClassReaders.setReader(new DefaultJaxrsApiReader());

        // Set the swagger config options
        SwaggerConfig config = ConfigFactory.config();
        config.setApiVersion("1.0.3");
        // Hope not to set base path: config.setBasePath("http://localhost:8000");
    }
}
