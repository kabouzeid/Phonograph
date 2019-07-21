package com.kabouzeid.gramophone.glide.artistimage;

/**
 * Used to define the artist cover
 */
public class AlbumCover {

    private int year;

    private String filePath;

    public AlbumCover(int year, String filePath) {

        this.filePath = filePath;
        this.year = year;
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
