package no.api.atomizer.core;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

/**
 * Representation of stale cache channel groups. 
 */
public class StaleGroup {
    /**
     * Synthetic primary key
     */
    private String id;
    /**
     * Date in milliseconds
     */
    @JsonProperty
    private long updated;
    /**
     * Cache group path / identifier
     */
    @JsonProperty
    private String path;

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

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "StaleGroup{" +
                "id=" + id +
                ", updated=" + updated +
                ", path='" + path + '\'' +
                '}';
    }
}
