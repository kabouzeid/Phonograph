package com.kabouzeid.gramophone.lastfm.rest.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class LastFmAlbum {
    @Expose
    private Album album;

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public static class Album {
        @Expose
        private List<Image> image = new ArrayList<>();
        @Expose
        private Wiki wiki;

        public List<Image> getImage() {
            return image;
        }

        public void setImage(List<Image> image) {
            this.image = image;
        }

        public Wiki getWiki() { return wiki; }

        public void setWiki(Wiki wiki) {
            this.wiki = wiki;
        }

        public class Wiki {
            @Expose
            private String content;

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }

        public static class Image {
            @SerializedName("#text")
            @Expose
            private String Text;
            @Expose
            private String size;

            public String getText() {
                return Text;
            }

            public void setText(String Text) {
                this.Text = Text;
            }

            public String getSize() {
                return size;
            }

            public void setSize(String size) {
                this.size = size;
            }
        }
    }
}
