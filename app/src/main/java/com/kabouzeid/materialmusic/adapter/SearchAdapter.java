package com.kabouzeid.materialmusic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.model.SearchEntry;
import com.kabouzeid.materialmusic.ui.activities.SearchActivity;
import com.kabouzeid.materialmusic.util.Util;

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
            subTitle.setVisibility(View.GONE);
            convertView.setBackgroundColor(Util.resolveColor(getContext(), R.attr.colorPrimary));
        } else {
            subTitle.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        title.setText(item.getTitle());
        subTitle.setText(item.getSubTitle());
        item.loadImage(imageView);

        return convertView;
    }
}
