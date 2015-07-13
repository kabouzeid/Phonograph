package com.kabouzeid.gramophone.helper.menu;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.dialogs.SongDetailDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AbsTagEditorActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.io.File;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongMenuHelper {
    public static int MENU_RES = R.menu.menu_item_song;

    public static boolean handleMenuClick(@NonNull AppCompatActivity activity, @NonNull Song song, @NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_as_ringtone:
                MusicUtil.setRingtone(activity, song.id);
                return true;
            case R.id.action_share:
                activity.startActivity(Intent.createChooser(MusicUtil.createShareSongFileIntent(song), null));
                return true;
            case R.id.action_delete_from_disk:
                DeleteSongsDialog.create(song).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(song).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(song);
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(song);
                return true;
            case R.id.action_tag_editor:
                Intent tagEditorIntent = new Intent(activity, SongTagEditorActivity.class);
                tagEditorIntent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id);
                if (activity instanceof PaletteColorHolder)
                    tagEditorIntent.putExtra(AbsTagEditorActivity.EXTRA_PALETTE, ((PaletteColorHolder) activity).getPaletteColor());
                activity.startActivity(tagEditorIntent);
                return true;
            case R.id.action_details:
                File songFile = new File(song.data);
                SongDetailDialog.create(songFile).show(activity.getSupportFragmentManager(), "SONG_DETAILS");
                return true;
            case R.id.action_go_to_album:
                Pair[] albumPairs = null;
                if (activity instanceof AbsSlidingMusicPanelActivity)
                    albumPairs = ((AbsSlidingMusicPanelActivity) activity).getSharedViewsWithPlayPauseFab(null);
                NavigationUtil.goToAlbum(activity, song.albumId, albumPairs);
                return true;
            case R.id.action_go_to_artist:
                Pair[] artistPairs = null;
                if (activity instanceof AbsSlidingMusicPanelActivity)
                    artistPairs = ((AbsSlidingMusicPanelActivity) activity).getSharedViewsWithPlayPauseFab(null);
                NavigationUtil.goToArtist(activity, song.artistId, artistPairs);
                return true;
        }
        return false;
    }

    public static abstract class OnClickSongMenu implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        private AppCompatActivity activity;

        public OnClickSongMenu(@NonNull AppCompatActivity activity) {
            this.activity = activity;
        }

        public int getMenuRes() {
            return MENU_RES;
        }

        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            popupMenu.inflate(getMenuRes());
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return handleMenuClick(activity, getSong(), item);
        }

        public abstract Song getSong();
    }
}
