package com.kabouzeid.gramophone.glide.artistimage;

/**
 * Used to define the artist cover
 */
public class AlbumCover {
    private int albumId;
    private int year;
    private String filePath;

    public AlbumCover(int albumId, int year, String filePath) {
        this.albumId = albumId;
        this.year = year;
        this.filePath = filePath;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}