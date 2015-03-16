package com.kabouzeid.gramophone.adapter.songadapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.SongDetailDialogHelper;
import com.kabouzeid.gramophone.loader.SongFilePathLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by karim on 27.11.14.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    public static final String TAG = AlbumSongAdapter.class.getSimpleName();
    protected Activity activity;
    protected List<Song> dataSet;

    public SongAdapter(Activity activity, List<Song> objects) {
        this.activity = activity;
        dataSet = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Song song = dataSet.get(position);

        holder.songTitle.setText(song.title);
        holder.songInfo.setText(song.artistName);

        Picasso.with(activity)
                .load(MusicUtil.getAlbumArtUri(song.albumId))
                .placeholder(R.drawable.default_album_art)
                .into(holder.albumArt);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView songTitle;
        TextView songInfo;
        ImageView overflowButton;
        ImageView albumArt;

        public ViewHolder(View itemView) {
            super(itemView);
            songTitle = (TextView) itemView.findViewById(R.id.song_title);
            songInfo = (TextView) itemView.findViewById(R.id.song_info);
            albumArt = (ImageView) itemView.findViewById(R.id.album_art);
            overflowButton = (ImageView) itemView.findViewById(R.id.menu);
            overflowButton.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicPlayerRemote.openQueue(dataSet, getPosition(), true);
                }
            });
        }

        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            popupMenu.inflate(R.menu.menu_song);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_play_next:
                            MusicPlayerRemote.playNext(dataSet.get(getPosition()));
                            return true;
                        case R.id.action_add_to_current_playing:
                            MusicPlayerRemote.enqueue(dataSet.get(getPosition()));
                            return true;
                        case R.id.action_tag_editor:
                            Intent intent = new Intent(activity, SongTagEditorActivity.class);
                            intent.putExtra(AppKeys.E_ID, dataSet.get(getPosition()).id);
                            activity.startActivity(intent);
                            return true;
                        case R.id.action_details:
                            String songFilePath = SongFilePathLoader.getSongFilePath(activity, dataSet.get(getPosition()).id);
                            File songFile = new File(songFilePath);
                            SongDetailDialogHelper.getDialog(activity, songFile).show();
                            return true;
                        case R.id.action_go_to_album:
                            Pair[] albumPairs = new Pair[]{
                                    Pair.create(albumArt, activity.getResources().getString(R.string.transition_album_cover))
                            };
                            if (activity instanceof AbsFabActivity)
                                albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                            NavigationUtil.goToAlbum(activity, dataSet.get(getPosition()).albumId, albumPairs);
                            return true;
                        case R.id.action_go_to_artist:
                            Pair[] artistPairs = null;
                            if (activity instanceof AbsFabActivity)
                                artistPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(artistPairs);
                            NavigationUtil.goToArtist(activity, dataSet.get(getPosition()).artistId, artistPairs);
                            return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }
}
