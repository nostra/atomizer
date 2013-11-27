package no.api.atomizer.mongodb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.WriteConcern;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 *
 */
public class MongodbConfiguration {
    @NotNull
    @JsonProperty
    private String hosts = null; // NOSONAR

    @NotNull
    @JsonProperty
    private Integer port = null; // NOSONAR

    @NotNull
    @JsonProperty
    private String name = null; // NOSONAR

    @Nullable
    @JsonProperty
    private WriteConcern writeConcern; // NOSONAR


    public String[] getHosts() {
        return hosts.split(",");
    }

    public Integer getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public WriteConcern getWriteConcern() {
        return writeConcern;
    }
}
