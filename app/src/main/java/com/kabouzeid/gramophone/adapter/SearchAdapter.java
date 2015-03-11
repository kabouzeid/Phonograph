package com.kabouzeid.gramophone.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.SearchEntry;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.SearchActivity;

import java.util.List;

/**
 * Created by karim on 27.02.15.
 */
public class SearchAdapter extends ArrayAdapter<SearchEntry> {

    public SearchAdapter(Context context, List<SearchEntry> objects) {
        super(context, R.layout.item_search, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_search, parent, false);
        }

        SearchEntry item = getItem(position);

        final TextView title = (TextView) convertView.findViewById(R.id.title);
        final TextView subTitle = (TextView) convertView.findViewById(R.id.sub_title);
        final ImageView imageView = (ImageView) convertView.findViewById(R.id.image);

        if (item instanceof SearchActivity.LabelEntry) {
            title.setTypeface(null, Typeface.BOLD);
            subTitle.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.materialmusic_default_bar_color));
        } else if (item instanceof Song) {
            title.setTypeface(null, Typeface.NORMAL);
            subTitle.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            title.setTypeface(null, Typeface.NORMAL);
            subTitle.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        title.setText(item.getTitle());
        subTitle.setText(item.getSubTitle());

        imageView.setImageBitmap(null);
        item.loadImage(getContext(), imageView);

        return convertView;
    }
}
