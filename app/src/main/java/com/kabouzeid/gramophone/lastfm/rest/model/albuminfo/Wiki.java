
package com.kabouzeid.gramophone.lastfm.rest.model.albuminfo;

import com.google.gson.annotations.Expose;

public class Wiki {

    @Expose
    private String published;
    @Expose
    private String summary;
    @Expose
    private String content;

    /**
     * @return The published
     */
    public String getPublished() {
        return published;
    }

    /**
     * @param published The published
     */
    public void setPublished(String published) {
        this.published = published;
    }

    /**
     * @return The summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary The summary
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return The content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content The content
     */
    public void setContent(String content) {
        this.content = content;
    }

}
