package com.kabouzeid.gramophone.adapter.artist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.lastfm.rest.model.artistinfo.Image;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistGridAdapter extends AbsArtistAdapter {
    public ArtistGridAdapter(@NonNull AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected Image getArtistImageToUse(List<Image> images) {
        return images.get(images.size() - 1);
    }
}
