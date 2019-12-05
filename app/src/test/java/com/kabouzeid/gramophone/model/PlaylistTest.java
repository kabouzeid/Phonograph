package com.kabouzeid.gramophone.model;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class PlaylistTest {
    Playlist playlist=new Playlist(5,"Test");

    @Test
     public void TestPlaylisthashCode() {
        int result=playlist.hashCode();
        assertEquals(2603341,result);
    }

    @Test
    public void TestPlaylisttoString() {
        String result=playlist.toString();
        assertEquals("Playlist{id=5, name='Test'}",result);
    }
}