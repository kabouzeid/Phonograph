package com.kabouzeid.gramophone.dialogs;

import android.util.Pair;

import androidx.fragment.app.DialogFragment;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.CalendarUtil;

public class DialogFactory {
    private static DialogFactory dialogFactory;

    public static DialogFactory getInstance(){
        if(dialogFactory == null) dialogFactory = new DialogFactory();
        return dialogFactory;
    }

    public Pair<DialogFragment,String> getDialogInstanceAndTag(int itemId, Song song){
        switch (itemId){
            case R.id.action_sleep_timer:
                return Pair.create(new SleepTimerDialog(),"SET_SLEEP_TIMER");
            case R.id.action_share:
                return Pair.create(SongShareDialog.create(song),"SHARE_SONG");
            case R.id.action_add_to_playlist:
                return Pair.create(AddToPlaylistDialog.create(song),"ADD_PLAYLIST");
            case R.id.action_save_playing_queue:
                return Pair.create(CreatePlaylistDialog.create(MusicPlayerRemote.getPlayingQueue()),"ADD_TO_PLAYLIST");
            default:
                return Pair.create(SongDetailDialog.create(song),"SONG_DETAIL");
        }
    }
}
