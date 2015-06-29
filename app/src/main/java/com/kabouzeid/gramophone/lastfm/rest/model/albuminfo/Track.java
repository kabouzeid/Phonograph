
package com.kabouzeid.gramophone.lastfm.rest.model.albuminfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Track {

    @Expose
    private String name;
    @Expose
    private String duration;
    @Expose
    private String mbid;
    @Expose
    private String url;
    @Expose
    private Streamable streamable;
    @Expose
    private Artist artist;
    @SerializedName("@attr")
    @Expose
    private com.kabouzeid.gramophone.lastfm.rest.model.albuminfo.Attr Attr;

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
     * @return The duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * @param duration The duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
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
     * @return The streamable
     */
    public Streamable getStreamable() {
        return streamable;
    }

    /**
     * @param streamable The streamable
     */
    public void setStreamable(Streamable streamable) {
        this.streamable = streamable;
    }

    /**
     * @return The artist
     */
    public Artist getArtist() {
        return artist;
    }

    /**
     * @param artist The artist
     */
    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    /**
     * @return The Attr
     */
    public com.kabouzeid.gramophone.lastfm.rest.model.albuminfo.Attr getAttr() {
        return Attr;
    }

    /**
     * @param Attr The @attr
     */
    public void setAttr(com.kabouzeid.gramophone.lastfm.rest.model.albuminfo.Attr Attr) {
        this.Attr = Attr;
    }

}
