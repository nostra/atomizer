package no.api.atomizer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import no.api.atomizer.mongodb.MongodbConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *
 */
public class AtomizerConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private MongodbConfiguration mongo = new MongodbConfiguration();

    public MongodbConfiguration getMongoConfiguration() {
        return mongo;
    }

}
