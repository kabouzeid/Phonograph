package com.kabouzeid.gramophone.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Category;
import com.kabouzeid.gramophone.util.SwipeAndDragHelper;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> implements SwipeAndDragHelper.ActionCompletionContract {
    private ArrayList<Category> categories;
    private ItemTouchHelper touchHelper;

    public CategoryAdapter(ArrayList<Category> categories) {
        this.categories = categories;
    }

    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_dialog_library_categories_listitem, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(CategoryAdapter.ViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.checkBox.setChecked(category.visible);
        holder.title.setText(holder.title.getResources().getString(category.id.key));

        holder.itemView.setOnClickListener(v -> {
            category.visible = !category.visible;
            holder.checkBox.setChecked(category.visible);
        });

        holder.dragView.setOnTouchListener((view, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        touchHelper.startDrag(holder);
                    }
                    return false;
                }
        );
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }


    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        Category category = categories.get(oldPosition);
        categories.remove(oldPosition);
        categories.add(newPosition, category);
        notifyItemMoved(oldPosition, newPosition);
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView title;
        public View dragView;

        public ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkbox);
            title = view.findViewById(R.id.title);
            dragView = view.findViewById(R.id.drag_view);
        }
    }
}

