package com.kabouzeid.gramophone.adapter.artist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.CabHolder;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistListAdapter extends AbsArtistAdapter {
    public ArtistListAdapter(@NonNull AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }
}
