package com.kabouzeid.gramophone.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.model.SearchEntry;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.SearchActivity;
import com.kabouzeid.gramophone.util.Util;

import java.util.List;

/**
 * Created by karim on 27.02.15.
 */
public class SearchAdapter extends ArrayAdapter<SearchEntry>{
    private Activity activity;

    public SearchAdapter(Activity activity, List<SearchEntry> objects) {
        super(activity, R.layout.item_list_search, objects);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_search, parent, false);
        }

        final SearchEntry item = getItem(position);

        final TextView title = (TextView) convertView.findViewById(R.id.title);
        final TextView subTitle = (TextView) convertView.findViewById(R.id.sub_title);
        final ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        final ImageView overflowButton = (ImageView) convertView.findViewById(R.id.menu);

        if (item instanceof SearchActivity.LabelEntry) {
            title.setTypeface(null, Typeface.BOLD);
            subTitle.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            overflowButton.setVisibility(View.GONE);
            convertView.setBackgroundColor(Util.resolveColor(getContext(), R.attr.default_bar_color));
        } else if (item instanceof Song) {
            title.setTypeface(null, Typeface.NORMAL);
            subTitle.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.TRANSPARENT);
            overflowButton.setVisibility(View.VISIBLE);
            overflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.inflate(R.menu.menu_item_song);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            return MenuItemClickHelper.handleSongMenuClick(activity, (Song) item, menuItem);
                        }
                    });
                    popupMenu.show();
                }
            });
        } else {
            title.setTypeface(null, Typeface.NORMAL);
            subTitle.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            overflowButton.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        title.setText(item.getTitle());
        subTitle.setText(item.getSubTitle());

        imageView.setImageBitmap(null);
        item.loadImage(getContext(), imageView);

        return convertView;
    }
}
