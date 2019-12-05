package com.kabouzeid.gramophone.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SongTest {
    Song song=new Song(1, "Super SOnga", 42, 2015, 233, "Aloha", 233, 45, "NajSongy", 10, "Krimes");

    @Test
    void TestSonghashCode() {
        int result=song.hashCode();
        assertEquals(-964081879,result);
    }

    @Test
    void TestSongtoString() {
        String result=song.toString();
        assertEquals("Song{id=1, title='Super SOnga', trackNumber=42, year=2015, duration=233, data='Aloha', dateModified=233, albumId=45, albumName='NajSongy', artistId=10, artistName='Krimes'}",result);
    }
}