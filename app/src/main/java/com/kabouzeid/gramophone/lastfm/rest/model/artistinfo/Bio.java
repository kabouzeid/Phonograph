
package com.kabouzeid.gramophone.lastfm.rest.model.artistinfo;

import com.google.gson.annotations.Expose;

public class Bio {

    @Expose
    private Links links;
    @Expose
    private String published;
    @Expose
    private String summary;
    @Expose
    private String content;
    @Expose
    private String yearformed;
    @Expose
    private Formationlist formationlist;

    /**
     * @return The links
     */
    public Links getLinks() {
        return links;
    }

    /**
     * @param links The links
     */
    public void setLinks(Links links) {
        this.links = links;
    }

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

    /**
     * @return The formationlist
     */
    public Formationlist getFormationlist() {
        return formationlist;
    }

    /**
     * @param formationlist The formationlist
     */
    public void setFormationlist(Formationlist formationlist) {
        this.formationlist = formationlist;
    }

}
