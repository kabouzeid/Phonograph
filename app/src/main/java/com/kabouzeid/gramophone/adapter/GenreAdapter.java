package com.kabouzeid.gramophone.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.model.Genre;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    @NonNull
    private final AppCompatActivity activity;
    private ArrayList<Genre> dataSet;
    private int itemLayoutRes;

    public GenreAdapter(@NonNull AppCompatActivity activity, ArrayList<Genre> dataSet, @LayoutRes int itemLayoutRes) {
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
    }

    public ArrayList<Genre> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(ArrayList<Genre> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Genre genre = dataSet.get(position);

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.separator != null) {
                holder.separator.setVisibility(View.GONE);
            }
        } else {
            if (holder.separator != null) {
                holder.separator.setVisibility(View.VISIBLE);
            }
        }
        if (holder.shortSeparator != null) {
            holder.shortSeparator.setVisibility(View.GONE);
        }
        if (holder.menu != null) {
            holder.menu.setVisibility(View.GONE);
        }
        if (holder.title != null) {
            holder.title.setText(genre.name);
        }
        if (holder.text != null) {
            holder.text.setText(MusicUtil.getGenreInfoString(activity, genre));
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        final Genre genre = dataSet.get(position);
        return genre.id == -1 ? "" : MusicUtil.getSectionName(dataSet.get(position).name);
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View view) {
            Genre genre = dataSet.get(getAdapterPosition());
            NavigationUtil.goToGenre(activity, genre);
        }
    }
}
