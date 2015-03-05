package com.kabouzeid.gramophone.helper;

import com.kabouzeid.gramophone.model.Song;

import java.util.Collections;
import java.util.List;

/**
 * Created by karim on 24.01.15.
 */
public class ShuffleHelper {
    public static void makeShuffleList(List<Song> listToShuffle, final int current) {
        if (current >= 0) {
            Song song = listToShuffle.remove(current);
            Collections.shuffle(listToShuffle);
            listToShuffle.add(0, song);
        } else {
            Collections.shuffle(listToShuffle);
        }
    }
}
