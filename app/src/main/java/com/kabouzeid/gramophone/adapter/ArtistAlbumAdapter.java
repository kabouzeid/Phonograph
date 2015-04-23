package com.kabouzeid.gramophone.adapter;

import android.app.Activity;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.signature.StringSignature;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAlbumAdapter extends RecyclerView.Adapter<ArtistAlbumAdapter.ViewHolder> {
    public static final String TAG = AlbumAdapter.class.getSimpleName();

    private static final int TYPE_FIRST = 1;
    private static final int TYPE_MIDDLE = 2;
    private static final int TYPE_LAST = 3;

    private final Activity activity;
    private final List<Album> dataSet;
    private final int listMargin;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_grid_artist_album, parent, false);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (viewType == TYPE_FIRST) {
            params.leftMargin = listMargin;
        } else if (viewType == TYPE_LAST) {
            params.rightMargin = listMargin;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        Object tag = holder.albumArt.getTag();
        if (tag instanceof Request) {
            ((Request) tag).clear();
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        holder.albumArt.setTag(Glide.with(activity)
                        .loadFromMediaStore(MusicUtil.getAlbumArtUri(album.id))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .signature(new StringSignature(album.albumArtPath))
                        .error(R.drawable.default_album_art)
                        .placeholder(R.drawable.default_album_art)
                        .into(holder.albumArt)
                        .getRequest()
        );

        holder.title.setText(album.title);
        holder.year.setText(String.valueOf(album.year));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_FIRST;
        } else if (position == getItemCount() - 1) {
            return TYPE_LAST;
        } else return TYPE_MIDDLE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView albumArt;
        final TextView title;
        final TextView year;

        public ViewHolder(View itemView) {
            super(itemView);
            albumArt = (ImageView) itemView.findViewById(R.id.album_art);
            title = (TextView) itemView.findViewById(R.id.album_title);
            year = (TextView) itemView.findViewById(R.id.album_year);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Pair[] albumPairs = new Pair[]{
                    Pair.create(albumArt,
                            activity.getResources().getString(R.string.transition_album_cover)
                    )};
            if (activity instanceof AbsFabActivity)
                albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
            NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).id, albumPairs);
        }
    }

    public ArtistAlbumAdapter(Activity activity, List<Album> objects) {
        this.activity = activity;
        dataSet = objects;
        listMargin = activity.getResources().getDimensionPixelSize(R.dimen.default_item_margin);
    }
}
