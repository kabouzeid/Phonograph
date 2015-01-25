package com.kabouzeid.materialmusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.model.Song;

import java.util.List;

/**
 * Created by karim on 24.01.15.
 */
public class PlayListAdapter extends ArrayAdapter<Song> {
    private Context context;
    private App app;

    public PlayListAdapter(Context context, List<Song> playList) {
        super(context, R.layout.item_playlist, playList);
        this.context = context;
        app = (App) context.getApplicationContext();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        }
        final TextView title = (TextView) convertView.findViewById(R.id.song_title);
        final ImageView albumArt = (ImageView) convertView.findViewById(R.id.album_art);

        title.setText(song.title);
        if (app.getMusicPlayerRemote().getCurrentSongId() == song.id) {
            albumArt.setImageResource(R.drawable.ic_speaker_white_48dp);
        } else {
            albumArt.setImageBitmap(null);
        }
        return convertView;
    }
}
