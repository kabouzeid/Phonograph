package com.kabouzeid.gramophone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.NavigationDrawerItem;
import com.kabouzeid.gramophone.util.Util;

import java.util.List;

/**
 * Created by karim on 23.11.14.
 */
public class NavigationDrawerItemAdapter extends ArrayAdapter<NavigationDrawerItem> {
    private int currentChecked = -1;

    public NavigationDrawerItemAdapter(Context context, int resource, List<NavigationDrawerItem> objects) {
        super(context, resource, objects);
    }

    public void setChecked(int position) {
        currentChecked = position;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NavigationDrawerItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_navigation_drawer, parent, false);
        }
        TextView title = (TextView) convertView.findViewById(R.id.title);
        ImageView icon = (ImageView) convertView.findViewById(R.id.album_art);
        title.setText(item.title);
        if (position == currentChecked) {
            title.setTextColor(Util.resolveColor(getContext(), R.attr.colorAccent));
            icon.setImageDrawable(Util.getTintedDrawable(getContext().getResources(), item.imageRes, Util.resolveColor(getContext(), R.attr.colorAccent)));
        } else {
            title.setTextColor(Util.resolveColor(getContext(), R.attr.title_text_color));
            icon.setImageDrawable(Util.getTintedDrawable(getContext().getResources(), item.imageRes, Util.resolveColor(getContext(), R.attr.themed_drawable_color)));
        }
        View container = convertView.findViewById(R.id.container);
        container.setActivated(position == currentChecked);
        return convertView;
    }
}
