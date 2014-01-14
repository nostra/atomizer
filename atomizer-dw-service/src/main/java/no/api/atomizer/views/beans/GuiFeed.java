package no.api.atomizer.views.beans;

import java.util.Date;
import java.util.List;

/**
 * Definition and holder of feed to be presented in the jsp GUI
 */
public class GuiFeed {
    private String title;
    private String id;
    private String self;
    private String prevArchive;
    private Date updated;
    private String author;
    private List<GuiEntry> entries;
    private int precision;
    private int lifetime;
    private String searchExpression;

    public String getSearchExpression() {
        return searchExpression;
    }

    public void setSearchExpression(String searchExpression) {
        this.searchExpression = searchExpression;
    }

    public int getPrecision() {
        return precision;
    }

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getPrevArchive() {
        return prevArchive;
    }

    public void setPrevArchive(String prevArchive) {
        this.prevArchive = prevArchive;
    }

    public Date getUpdated() {
        return updated; // NOSONAR Accepting that date is mutable
    }

    public void setUpdated(Date updated) {
        this.updated = updated; // NOSONAR Accepting that date is mutable
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<GuiEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<GuiEntry> entries) {
        this.entries = entries;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }
}
