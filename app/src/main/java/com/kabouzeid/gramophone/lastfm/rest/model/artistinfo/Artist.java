
package com.kabouzeid.gramophone.lastfm.rest.model.artistinfo;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Artist {

    @Expose
    private String name;
    @Expose
    private String mbid;
    @Expose
    private String url;
    @Expose
    private List<Image> image = new ArrayList<Image>();
    @Expose
    private String ontour;
    @Expose
    private Stats stats;
    @Expose
    private Bio bio;

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The mbid
     */
    public String getMbid() {
        return mbid;
    }

    /**
     * @param mbid The mbid
     */
    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The image
     */
    public List<Image> getImage() {
        return image;
    }

    /**
     * @param image The image
     */
    public void setImage(List<Image> image) {
        this.image = image;
    }

    /**
     * @return The ontour
     */
    public String getOntour() {
        return ontour;
    }

    /**
     * @param ontour The ontour
     */
    public void setOntour(String ontour) {
        this.ontour = ontour;
    }

    /**
     * @return The stats
     */
    public Stats getStats() {
        return stats;
    }

    /**
     * @param stats The stats
     */
    public void setStats(Stats stats) {
        this.stats = stats;
    }

    /**
     * @return The bio
     */
    public Bio getBio() {
        return bio;
    }

    /**
     * @param bio The bio
     */
    public void setBio(Bio bio) {
        this.bio = bio;
    }

}
