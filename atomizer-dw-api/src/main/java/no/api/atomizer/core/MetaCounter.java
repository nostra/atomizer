package no.api.atomizer.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

/**
 *
 */
public class MetaCounter {
    /**
     * Synthetic primary key
     */
    private String id;
    /**
     * Token name for which to maintain counter
     */
    @JsonProperty
    private String token;

    @JsonProperty
    private Integer counter;

    @ObjectId
    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @ObjectId
    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }
}
