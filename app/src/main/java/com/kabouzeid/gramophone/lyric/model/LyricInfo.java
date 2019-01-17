package com.kabouzeid.gramophone.lyric.model;

/**
 * Information about lyrics
 */
public class LyricInfo {

    private String url;

    private String name;

    private String title;

    private String artist;

    private String album;

    private String uploader;

    private String timelength;

    private Double rate;

    private int downloadscount;

    private int ratecount;

    public String getLyricURL() {
        return url;
    }

    public String getLyricsFileName() {
        return name;
    }

    public String getMusicTitle() {
        return title;
    }

    public String getMusicArtist() {
        return artist;
    }

    public String getMusicAlbum() {
        return album;
    }

    public String getLyricUploader() {
        return uploader;
    }

    public Double getLyricRate() {
        return rate;
    }

    public Integer getLyricRatesCount() {
        return ratecount;
    }

    public Integer getLyricDownloadsCount() {
        return downloadscount;
    }

    public String getMusicLenght() {
        return timelength;
    }

    public void setLyricURL(String value) {
        url = value;
    }

    public void setLyricsFileName(String value) {
        name = value;
    }

    public void setMusicTitle(String value) {
        title = value;
    }

    public void setMusicArtist(String value) {
        artist = value;
    }

    public void setMusicAlbum(String value) {
        album = value;
    }

    public void setLyricUploader(String value) {
        uploader = value;
    }

    public void setLyricRate(Double value) {
        rate = value;
    }

    public void setLyricRatesCount(Integer i) {
        ratecount = i;
    }

    public void setLyricDownloadsCount(Integer value) {
        downloadscount = value;
    }

    public void setMusicLenght(String value) {
        timelength = value;
    }

    public String dump() {
        return String.format(
                "<fileinfo filetype=\"lyrics\" link=\"%s\" filename=\"%s\" artist=\"%s\" title=\"%s\" album=\"%s\" uploader=\"%s\" rate=\"%f\" ratecount=\"%d\" downloads=\"%d\" timelength=\"%s\"/>",
                url, name, artist, title, album, uploader, rate, ratecount, downloadscount, timelength);
    }
}
