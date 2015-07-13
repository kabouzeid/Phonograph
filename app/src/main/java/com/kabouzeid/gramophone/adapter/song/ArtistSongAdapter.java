package com.kabouzeid.gramophone.adapter.song;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.menu.SongMenuHelper;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistSongAdapter extends ArrayAdapter<Song> implements MaterialCab.Callback {

    @Nullable
    private final CabHolder cabHolder;
    private MaterialCab cab;
    private ArrayList<Song> dataSet;
    private ArrayList<Song> checked;

    @NonNull
    private final AppCompatActivity activity;

    public ArtistSongAdapter(@NonNull AppCompatActivity activity, @NonNull ArrayList<Song> songs, @Nullable CabHolder cabHolder) {
        super(activity, R.layout.item_list, songs);
        this.activity = activity;
        this.cabHolder = cabHolder;
        checked = new ArrayList<>();
        dataSet = songs;
    }

    public ArrayList<Song> getDataSet() {
        return dataSet;
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list, parent, false);
        }

        final TextView songTitle = (TextView) convertView.findViewById(R.id.title);
        final TextView songInfo = (TextView) convertView.findViewById(R.id.text);
        final ImageView albumArt = (ImageView) convertView.findViewById(R.id.image);

        songTitle.setText(song.title);
        songInfo.setText(song.albumName);

        ImageLoader.getInstance().displayImage(
                MusicUtil.getSongImageLoaderString(song),
                albumArt,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .resetViewBeforeLoading(true)
                        .build()
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            albumArt.setTransitionName(activity.getString(R.string.transition_album_art));
        }

        final ImageView overflowButton = (ImageView) convertView.findViewById(R.id.menu);
        overflowButton.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
            @Override
            public Song getSong() {
                return song;
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_go_to_album) {
                    Pair[] albumPairs = new Pair[]{
                            Pair.create(albumArt, activity.getResources().getString(R.string.transition_album_art))
                    };
                    if (activity instanceof AbsSlidingMusicPanelActivity)
                        albumPairs = ((AbsSlidingMusicPanelActivity) activity).getSharedViewsWithPlayPauseFab(albumPairs);
                    NavigationUtil.goToAlbum(activity, song.albumId, albumPairs);
                    return true;
                }
                return super.onMenuItemClick(item);
            }
        });

        convertView.setActivated(isChecked(song));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInQuickSelectMode()) {
                    toggleChecked(song);
                } else {
                    MusicPlayerRemote.openQueue(dataSet, position, true);
                }
            }
        });
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                toggleChecked(song);
                return true;
            }
        });

        return convertView;
    }

    private void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Song> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_from_disk:
                DeleteSongsDialog.create(selection).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
                break;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(selection);
                break;
        }
    }

    protected void toggleChecked(Song song) {
        if (cabHolder != null) {
            openCabIfNecessary();

            if (!checked.remove(song)) checked.add(song);
            notifyDataSetChanged();

            final int size = checked.size();
            if (size <= 0) cab.finish();
            else if (size == 1) cab.setTitle(checked.get(0).toString());
            else if (size > 1) cab.setTitle(String.valueOf(size));
        }
    }

    private void openCabIfNecessary() {
        if (cabHolder != null) {
            if (cab == null || !cab.isActive()) {
                cab = cabHolder.openCab(R.menu.menu_media_selection, this);
            }
        }
    }

    private void unCheckAll() {
        checked.clear();
        notifyDataSetChanged();
    }

    protected boolean isChecked(Song song) {
        return checked.contains(song);
    }

    protected boolean isInQuickSelectMode() {
        return cab != null && cab.isActive();
    }

    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(@NonNull MenuItem menuItem) {
        onMultipleItemAction(menuItem, new ArrayList<>(checked));
        cab.finish();
        unCheckAll();
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab materialCab) {
        unCheckAll();
        return true;
    }
}
