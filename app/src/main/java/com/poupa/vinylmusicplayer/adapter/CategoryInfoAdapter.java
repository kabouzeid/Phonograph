package com.poupa.vinylmusicplayer.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.CategoryInfo;
import com.poupa.vinylmusicplayer.util.SwipeAndDragHelper;

import java.util.ArrayList;

public class CategoryInfoAdapter extends RecyclerView.Adapter<CategoryInfoAdapter.ViewHolder> implements SwipeAndDragHelper.ActionCompletionContract {
    private ArrayList<CategoryInfo> categoryInfos;
    private ItemTouchHelper touchHelper;

    public CategoryInfoAdapter(ArrayList<CategoryInfo> categoryInfos) {
        this.categoryInfos = categoryInfos;
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(this);
        touchHelper = new ItemTouchHelper(swipeAndDragHelper);
    }

    @Override
    public CategoryInfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_dialog_library_categories_listitem, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(CategoryInfoAdapter.ViewHolder holder, int position) {
        CategoryInfo categoryInfo = categoryInfos.get(position);

        holder.checkBox.setChecked(categoryInfo.visible);
        holder.title.setText(holder.title.getResources().getString(categoryInfo.category.stringRes));

        holder.itemView.setOnClickListener(v -> {
            if (!(categoryInfo.visible && isLastCheckedCategory(categoryInfo))) {
                categoryInfo.visible = !categoryInfo.visible;
                holder.checkBox.setChecked(categoryInfo.visible);
            }
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
        return categoryInfos.size();
    }


    public void setCategoryInfos(ArrayList<CategoryInfo> categoryInfos) {
        this.categoryInfos = categoryInfos;
        notifyDataSetChanged();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        CategoryInfo categoryInfo = categoryInfos.get(oldPosition);
        categoryInfos.remove(oldPosition);
        categoryInfos.add(newPosition, categoryInfo);
        notifyItemMoved(oldPosition, newPosition);
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        touchHelper.attachToRecyclerView(recyclerView);
    }

    public ArrayList<CategoryInfo> getCategoryInfos() {
        return categoryInfos;
    }

    private boolean isLastCheckedCategory(CategoryInfo categoryInfo) {
        if (categoryInfo.visible) {
            for (CategoryInfo c : categoryInfos) {
                if (c != categoryInfo && c.visible) return false;
            }
        }
        return true;
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

