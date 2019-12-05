package com.kabouzeid.gramophone;


import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArtistTest {

    @Test
    public void TestgetSongsList() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song(1, "Super SOnga", 42, 2015, 233, "Aloha", 233, 45, "NajSongy", 10, "Krimes"));
        Artist artist= Mockito.mock(Artist.class);
        Mockito.when(artist.getSongs()).thenReturn(songs);
        assertEquals(artist.getSongs(),songs);
    }
}