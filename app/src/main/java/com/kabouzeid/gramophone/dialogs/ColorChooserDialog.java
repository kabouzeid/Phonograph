package com.kabouzeid.gramophone.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.views.SelectableColorView;

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
public class ColorChooserDialog extends LeakDetectDialogFragment implements View.OnClickListener {

    private Colors colors;

    public ColorChooserDialog() {
    }

    private int mCircleSize;
    private ColorCallback mCallback;
    private GridView mGrid;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof ColorCallback))
            throw new IllegalStateException("ColorChooserDialog needs to be shown from an Activity implementing ColorCallback.");
        mCallback = (ColorCallback) activity;
    }

    private boolean isInSub() {
        return getArguments().getBoolean("in_sub", false);
    }

    private void setInSub(boolean value) {
        getArguments().putBoolean("in_sub", value);
        if (value) {
            ((MaterialDialog) getDialog()).setActionButton(DialogAction.NEUTRAL, R.string.back);
        } else {
            ((MaterialDialog) getDialog()).setActionButton(DialogAction.NEUTRAL, null);
        }
    }

    private int getTopIndex() {
        return getArguments().getInt("top_index", -1);
    }

    private void setTopIndex(int value) {
        if (getTopIndex() != value)
            setSubIndex(colors.headerColorIndexes[value]);
        getArguments().putInt("top_index", value);
    }

    private int getSubIndex() {
        return getArguments().getInt("sub_index", -1);
    }

    private void setSubIndex(int value) {
        getArguments().putInt("sub_index", value);
    }

    private int getPreselectColor() {
        return getArguments().getInt("color_preselect", -1);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            final int index = (Integer) v.getTag();
            if (isInSub()) {
                setSubIndex(index);
            } else {
                setTopIndex(index);
                setInSub(true);
            }
            invalidateGrid();
        }
    }

    public interface ColorCallback {
        void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor);
    }

    private void setColors() {
        colors = Colors.fromBundle(getArguments());
    }

    private void setIndexesFor(@ColorInt int color) {
        if (getTopIndex() != -1) return;
        if (color != -1) {
            for (int i = 0; i < colors.colors.length; i++) {
                for (int z = 0; z < colors.colors[i].length; z++) {
                    if (color == colors.colors[i][z]) {
                        setTopIndex(i);
                        setSubIndex(z);
                        return;
                    }
                }
            }
        }
    }

    public int getTitleRes() {
        return getArguments().getInt("title", 0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setColors();
        setIndexesFor(getPreselectColor());

        final DisplayMetrics dm = getResources().getDisplayMetrics();
        mCircleSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, dm);
        mGrid = new GridView(getContext());
        mGrid.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mGrid.setColumnWidth(mCircleSize);
        mGrid.setNumColumns(GridView.AUTO_FIT);

        final int eightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dm);
        mGrid.setVerticalSpacing(eightDp);
        mGrid.setHorizontalSpacing(eightDp);

        final int sixteenDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, dm);
        mGrid.setPadding(sixteenDp, sixteenDp, sixteenDp, sixteenDp);
        mGrid.setClipToPadding(false);
        mGrid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        mGrid.setGravity(Gravity.CENTER);

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(getTitleRes())
                .autoDismiss(false)
                .customView(mGrid, false)
                .positiveText(R.string.select)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        mCallback.onColorSelection(ColorChooserDialog.this, getSelectedColor());
                        dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);
                        setInSub(false);
                        invalidateGrid();
                    }
                }).build();
        invalidateGrid();
        if (isInSub()) {
            dialog.setActionButton(DialogAction.NEUTRAL, R.string.back);
        }
        return dialog;
    }

    @ColorInt
    private int getSelectedColor() {
        int selectedColor = 0;
        int topIndex = getTopIndex();
        int subIndex = getSubIndex();
        if (topIndex != -1 && subIndex != -1) {
            selectedColor = colors.colors[topIndex][subIndex];
        }
        return selectedColor;
    }

    private void invalidateGrid() {
        if (mGrid.getAdapter() == null) {
            mGrid.setAdapter(new ColorGridAdapter());
        } else {
            ((BaseAdapter) mGrid.getAdapter()).notifyDataSetChanged();
        }
    }

    private class ColorGridAdapter extends BaseAdapter {

        public ColorGridAdapter() {
        }

        @Override
        public int getCount() {
            if (isInSub()) {
                return colors.colors[getTopIndex()].length;
            } else {
                return colors.colors.length;
            }
        }

        @Override
        public Object getItem(int position) {
            if (isInSub()) {
                return colors.colors[getTopIndex()][position];
            } else {
                return colors.colors[position][colors.headerColorIndexes[position]];
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new SelectableColorView(getContext());
                convertView.setLayoutParams(new GridView.LayoutParams(mCircleSize, mCircleSize));
            }
            SelectableColorView child = (SelectableColorView) convertView;
            if (isInSub()) {
                child.setBackgroundColor(colors.colors[getTopIndex()][position]);
                child.setSelected(getSubIndex() == position);
            } else {
                child.setBackgroundColor(colors.colors[position][colors.headerColorIndexes[position]]);
                child.setSelected(getTopIndex() == position);
            }
            child.setTag(position);
            child.setOnClickListener(ColorChooserDialog.this);
            return convertView;
        }
    }

    public static ColorChooserDialog create(@StringRes int title, @NonNull Colors colors, @ColorInt int preselectColor) {
        ColorChooserDialog dialog = new ColorChooserDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("color_preselect", preselectColor);

        Colors.toBundle(colors, args);

        dialog.setArguments(args);
        return dialog;
    }

    public static final class Colors {
        final int[] headerColorIndexes;
        final int[][] colors;

        public Colors(int[] headerColorIndexes, int[][] colors) {
            if (headerColorIndexes.length != colors.length) {
                throw new IllegalArgumentException("int[] headerColorIndexes and int[][] colors must have the same length");
            }
            this.headerColorIndexes = headerColorIndexes;
            this.colors = colors;
        }

        static void toBundle(Colors colors, Bundle bundle) {
            bundle.putIntArray("top_colors", colors.headerColorIndexes);
            for (int i = 0; i < colors.colors.length; i++) {
                bundle.putIntArray("sub_colors_" + i, colors.colors[i]);
            }
        }

        static Colors fromBundle(Bundle bundle) {
            int[] headerColorIndexes = bundle.getIntArray("top_colors");
            if (headerColorIndexes == null) return new Colors(new int[]{}, new int[][]{});
            int[][] colors = new int[headerColorIndexes.length][];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = bundle.getIntArray("sub_colors_" + i);
            }
            return new Colors(headerColorIndexes, colors);
        }
    }
}
