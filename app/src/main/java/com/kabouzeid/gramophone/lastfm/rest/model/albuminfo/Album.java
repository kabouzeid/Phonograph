
package com.kabouzeid.gramophone.lastfm.rest.model.albuminfo;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Album {

    @Expose
    private String name;
    @Expose
    private String artist;
    @Expose
    private String id;
    @Expose
    private String mbid;
    @Expose
    private String url;
    @Expose
    private String releasedate;
    @Expose
    private List<Image> image = new ArrayList<Image>();
    @Expose
    private String listeners;
    @Expose
    private String playcount;
    @Expose
    private Tracks tracks;
    @Expose
    private Toptags toptags;
    @Expose
    private Wiki wiki;

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
     * @return The artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @param artist The artist
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
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
     * @return The releasedate
     */
    public String getReleasedate() {
        return releasedate;
    }

    /**
     * @param releasedate The releasedate
     */
    public void setReleasedate(String releasedate) {
        this.releasedate = releasedate;
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
     * @return The listeners
     */
    public String getListeners() {
        return listeners;
    }

    /**
     * @param listeners The listeners
     */
    public void setListeners(String listeners) {
        this.listeners = listeners;
    }

    /**
     * @return The playcount
     */
    public String getPlaycount() {
        return playcount;
    }

    /**
     * @param playcount The playcount
     */
    public void setPlaycount(String playcount) {
        this.playcount = playcount;
    }

    /**
     * @return The tracks
     */
    public Tracks getTracks() {
        return tracks;
    }

    /**
     * @param tracks The tracks
     */
    public void setTracks(Tracks tracks) {
        this.tracks = tracks;
    }

    /**
     * @return The toptags
     */
    public Toptags getToptags() {
        return toptags;
    }

    /**
     * @param toptags The toptags
     */
    public void setToptags(Toptags toptags) {
        this.toptags = toptags;
    }

    /**
     * @return The wiki
     */
    public Wiki getWiki() {
        return wiki;
    }

    /**
     * @param wiki The wiki
     */
    public void setWiki(Wiki wiki) {
        this.wiki = wiki;
    }

}
