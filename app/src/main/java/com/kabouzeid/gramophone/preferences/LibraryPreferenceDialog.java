package com.kabouzeid.gramophone.preferences;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.CategoryAdapter;
import com.kabouzeid.gramophone.model.Category;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.SwipeAndDragHelper;

import java.util.ArrayList;


public class LibraryPreferenceDialog extends DialogFragment {
    public static LibraryPreferenceDialog newInstance() {
        return new LibraryPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.preference_dialog_library_categories, null);

        final ArrayList<Category> categories = PreferenceUtil.getInstance(getContext()).getLibraryCategories();
        RecyclerView categoriesView = view.findViewById(R.id.recycler_view);
        categoriesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        final CategoryAdapter adapter = new CategoryAdapter(categories, getCheckboxColors());
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        adapter.setTouchHelper(touchHelper);
        categoriesView.setAdapter(adapter);
        touchHelper.attachToRecyclerView(categoriesView);

        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(R.string.library_categories)
                .customView(view, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.reset_action)
                .autoDismiss(false)
                .onNeutral((dialog1, action) -> {
                    adapter.setCategories(PreferenceUtil.getInstance(getContext()).getDefaultLibraryCategories());
                })
                .onNegative((dialog12, action) -> dismiss())
                .onPositive((dialog13, action) -> {
                    if (!updateCategories(adapter.getCategories())) {
                        new MaterialDialog.Builder(getContext())
                            .title(R.string.edit_categories)
                            .content(R.string.at_least_one_category_must_be_enabled)
                            .positiveText(android.R.string.ok)
                            .show();
                    } else {
                        dismiss();
                    }
                })
                .build();

        return dialog;
    }

    private boolean updateCategories(ArrayList<Category> categories) {
        if (getSelected(categories) == 0) return false;

        PreferenceUtil.getInstance(getContext()).setLibraryCategories(categories);

        return true;
    }

    private int getSelected(ArrayList<Category> categories) {
        int selected = 0;
        for (Category category : categories) {
            if (category.visible)
                selected ++;
        }
        return selected;
    }

    private ColorStateList getCheckboxColors() {
        int disabledColor = DialogUtils.getDisabledColor(getContext());
        return new ColorStateList(
                new int[][] {
                    new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked},
                    new int[] {android.R.attr.state_enabled, android.R.attr.state_checked},
                    new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked},
                    new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked}
                },
                new int[] {
                    DialogUtils.resolveColor(getContext(), R.attr.colorControlNormal),
                    ThemeStore.accentColor(getContext()),
                    disabledColor,
                    disabledColor
                });
    }
}
