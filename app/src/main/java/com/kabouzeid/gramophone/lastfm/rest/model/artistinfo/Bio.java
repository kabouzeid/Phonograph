
package com.kabouzeid.gramophone.lastfm.rest.model.artistinfo;

import com.google.gson.annotations.Expose;

public class Bio {

    @Expose
    private String published;
    @Expose
    private String summary;
    @Expose
    private String content;
    @Expose
    private String placeformed;
    @Expose
    private String yearformed;

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

    /**
     * @return The placeformed
     */
    public String getPlaceformed() {
        return placeformed;
    }

    /**
     * @param placeformed The placeformed
     */
    public void setPlaceformed(String placeformed) {
        this.placeformed = placeformed;
    }

    /**
     * @return The yearformed
     */
    public String getYearformed() {
        return yearformed;
    }

    /**
     * @param yearformed The yearformed
     */
    public void setYearformed(String yearformed) {
        this.yearformed = yearformed;
    }

}
