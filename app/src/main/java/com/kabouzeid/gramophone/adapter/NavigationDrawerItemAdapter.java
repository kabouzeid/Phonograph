package com.kabouzeid.gramophone.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.ColorChooserDialog;
import com.kabouzeid.gramophone.model.NavigationDrawerItem;
import com.kabouzeid.gramophone.ui.fragments.NavigationDrawerFragment;

import java.util.ArrayList;

/**
 * @author Aidan Follestad (afollestad)
 */
public class NavigationDrawerItemAdapter extends RecyclerView.Adapter<NavigationDrawerItemAdapter.ShortcutViewHolder> implements View.OnClickListener {

    // per the Material design guidelines
    @SuppressWarnings("FieldCanBeLocal")
    private final int ALPHA_ACTIVATED = 255;
    @SuppressWarnings("FieldCanBeLocal")
    private final int ALPHA_ICON = 138;
    @SuppressWarnings("FieldCanBeLocal")
    private final int ALPHA_TEXT = 222;

    @Override
    public void onClick(View v) {
        int index = (Integer) v.getTag();
        if (mCallback != null)
            mCallback.onItemSelected(index);
    }

    public static class ShortcutViewHolder extends RecyclerView.ViewHolder {

        public ShortcutViewHolder(View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.divider);
            container = itemView.findViewById(R.id.container);
            title = (TextView) itemView.findViewById(R.id.title);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }

        final TextView title;
        final ImageView icon;
        final View divider;
        final View container;
    }

    private int currentChecked = -1;
    private int navIconColor;
    private final ArrayList<NavigationDrawerItem> mItems;
    private final Callback mCallback;

    public interface Callback {
        void onItemSelected(int index);
    }

    public NavigationDrawerItemAdapter(Context context, ArrayList<NavigationDrawerItem> objects, Callback callback) {
        navIconColor = DialogUtils.resolveColor(context, R.attr.nav_drawer_icon_color);
        if (DialogUtils.isColorDark(navIconColor))
            navIconColor = ColorChooserDialog.shiftColorUp(navIconColor);
        mItems = objects;
        mCallback = callback;
    }

    public void setChecked(int position) {
//        int oldPosition = currentChecked;
        currentChecked = position;
        notifyDataSetChanged();
    }

    @Override
    public ShortcutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation_drawer, parent, false);
        return new ShortcutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShortcutViewHolder holder, int position) {
        NavigationDrawerItem item = mItems.get(position);

        holder.title.setText(item.title);
        holder.icon.setImageResource(item.imageRes);
        holder.divider.setVisibility(position == NavigationDrawerFragment.SETTINGS_INDEX ?
                View.VISIBLE : View.GONE);

        final boolean selected = position == currentChecked;
        final int iconColor = selected ? ThemeSingleton.get().positiveColor : navIconColor;
        final int textColor = selected ? ThemeSingleton.get().positiveColor : navIconColor;

        holder.title.setTextColor(textColor);
        holder.title.setAlpha(selected ? ALPHA_ACTIVATED : ALPHA_TEXT);
        holder.icon.setColorFilter(iconColor, PorterDuff.Mode.SRC_ATOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.icon.setImageAlpha(selected ? ALPHA_ACTIVATED : ALPHA_ICON);
        } else {
            // noinspection deprecation
            holder.icon.setAlpha(selected ? ALPHA_ACTIVATED : ALPHA_ICON);
        }

        holder.container.setActivated(selected);
        holder.container.setTag(position);
        holder.container.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}