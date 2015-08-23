package com.kabouzeid.gramophone.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.views.ColorView;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ColorChooserDialog extends LeakDetectDialogFragment implements View.OnClickListener {

    private ColorCallback mCallback;
    private int[] mColors;
    private GridView mGrid;

    public ColorChooserDialog() {
    }

    private static int translucentColor(int color) {
        final float factor = 0.7f;
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @NonNull
    private static Drawable createSelector(int color) {
        ShapeDrawable darkerCircle = new ShapeDrawable(new OvalShape());
        darkerCircle.getPaint().setColor(translucentColor(ColorUtil.shiftColorDown(color)));
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, darkerCircle);
        return stateListDrawable;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (ColorCallback) activity;
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getTag() != null) {
            final int index = (Integer) v.getTag();
            getArguments().putInt("preselect", mColors[index]);
            invalidateGrid();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(getArguments().getInt("title", 0))
                .autoDismiss(false)
                .customView(R.layout.dialog_color_chooser, false)
                .neutralText(R.string.default_str)
                .positiveText(R.string.select)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        final int title = getArguments().getInt("title", 0);
                        final int preselect = getArguments().getInt("preselect", -1);
                        mCallback.onColorSelection(title, preselect);
                        dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);
                        if (getArguments().getInt("title", 0) == R.string.primary_color) {
                            getArguments().putInt("preselect", ContextCompat.getColor(getContext(), R.color.indigo_500));
                        } else if (getArguments().getInt("title", 0) == R.string.accent_color) {
                            getArguments().putInt("preselect", ContextCompat.getColor(getContext(), R.color.pink_A200));
                        }
                        invalidateGrid();
                    }
                })
                .build();

        final boolean primary = getArguments().getInt("title", 0) == R.string.primary_color;
        final TypedArray ta = getActivity().getResources().obtainTypedArray(
                primary ? R.array.colors_primary : R.array.colors_accent);
        mColors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++)
            mColors[i] = ta.getColor(i, 0);
        ta.recycle();
        mGrid = (GridView) dialog.getCustomView();
        if (mGrid != null) {
            mGrid.setNumColumns(primary ? 7 : 4);
            invalidateGrid();
        }
        return dialog;
    }

    private void invalidateGrid() {
        if (mGrid.getAdapter() == null) {
            mGrid.setAdapter(new ColorGridAdapter());
            mGrid.setSelector(ResourcesCompat.getDrawable(getResources(), R.drawable.md_transparent, null));
        } else ((BaseAdapter) mGrid.getAdapter()).notifyDataSetChanged();
    }

    public void show(@NonNull AppCompatActivity activity, @StringRes int title, int preselect) {
        Bundle args = new Bundle();
        args.putInt("preselect", preselect);
        args.putInt("title", title);
        setArguments(args);
        show(activity.getSupportFragmentManager(), "COLOR_SELECTOR");
    }

    public interface ColorCallback {
        void onColorSelection(int title, int color);
    }

    private class ColorGridAdapter extends BaseAdapter implements View.OnClickListener {

        public ColorGridAdapter() {
        }

        @Override
        public int getCount() {
            return mColors.length;
        }

        @Override
        public Object getItem(int position) {
            return mColors[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Nullable
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.griditem_color_chooser, parent, false);

            final ColorView colorView = (ColorView) convertView;
            colorView.setActivated(getArguments().getInt("preselect") == mColors[position]);
            colorView.setBackgroundColor(mColors[position]);
            colorView.setTag(position);
            colorView.setOnClickListener(this);

            Drawable selector = createSelector(mColors[position]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_pressed}
                };
                int[] colors = new int[]{
                        ColorUtil.shiftColorDown(mColors[position])
                };
                ColorStateList rippleColors = new ColorStateList(states, colors);
                colorView.setForeground(new RippleDrawable(rippleColors, selector, null));
            } else {
                colorView.setForeground(selector);
            }
            return convertView;
        }

        @Override
        public void onClick(@NonNull View v) {
            final int index = (Integer) v.getTag();
            getArguments().putInt("preselect", mColors[index]);
            invalidateGrid();
        }
    }
}