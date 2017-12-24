package com.kabouzeid.gramophone.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.SwipeAndDragHelper;
import com.kabouzeid.gramophone.model.Category;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SwipeAndDragHelper.ActionCompletionContract {
    private ArrayList<Category> categories;
    private ItemTouchHelper touchHelper;
    private ColorStateList color;

    public CategoryAdapter(ArrayList<Category> categories, ColorStateList color) {
        this.categories = copy(categories);
        this.color = color;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_dialog_library_categories_listitem, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Category category = categories.get(position);
        CategoryViewHolder h = (CategoryViewHolder) holder;
        h.checkBox.setChecked(category.visible);
        h.title.setText(h.title.getResources().getString(category.id.key));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            h.checkBox.setButtonTintList(color);
        } else {
            Drawable checkDrawable =
                    ContextCompat.getDrawable(h.checkBox.getContext(), R.drawable.abc_btn_check_material);
            Drawable drawable = DrawableCompat.wrap(checkDrawable);
            DrawableCompat.setTintList(drawable, color);
            h.checkBox.setButtonDrawable(drawable);
        }

        h.checkBox.setOnClickListener((view) -> {
                h.checkBox.setChecked(category.visible = !category.visible);
                notifyDataSetChanged();
            }
        );

        h.title.setOnClickListener((view) -> {
                h.checkBox.setChecked(category.visible = !category.visible);
                notifyDataSetChanged();
            }
        );

        h.dragHandle.setOnTouchListener((view, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        touchHelper.startDrag(h);
                    }
                    return false;
                }
        );

        Context context = h.dragHandle.getContext();
        int backgroundColor = ThemeStore.textColorSecondary(context);
        int borderColor = ThemeStore.textColorSecondaryInverse(context);
        int height = getPixel(1, h.dragHandle);

        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.RED, Color.RED});
        d.setStroke(height, backgroundColor);
        d.setSize(height * 4, getPixel(16, h.dragHandle));

        h.dragHandle.setBackground(new DragHandle(h.dragHandle.getResources().getDisplayMetrics().density, backgroundColor, borderColor));
    }

    private static class DragHandle extends Drawable {
        private float density;
        private Paint shape;
        private Paint outline;

        public DragHandle(float density, int color, int borderColor) {
            this.shape = new Paint();
            this.shape.setStyle(Paint.Style.FILL);
            this.shape.setColor(color);

            this.outline = new Paint();
            this.outline.setAntiAlias(true);
            this.outline.setStyle(Paint.Style.STROKE);
            this.outline.setColor(borderColor);

            this.density = density;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect bounds = getBounds();
            float width = 15 * density;
            float left = bounds.centerX() - width / 2;
            float top = bounds.top + bounds.centerY() - 3 * density;
            canvas.save();
            canvas.drawRect(left, top, left + width, top + 2 * density, shape);
            canvas.drawRect(left, top, left + width, top + 2 * density, outline);
            canvas.translate(0, (density * 2) * 2);
            canvas.drawRect(left, top, left + width, top + 2 * density, shape);
            canvas.drawRect(left, top, left + width, top + 2 * density, outline);
            canvas.restore();
        }

        @Override
        public void setAlpha(int i) {
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter filter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }


    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = copy(categories);
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


    private ArrayList<Category> copy(ArrayList<Category> categories) {
        ArrayList<Category> result = new ArrayList<>();
        for (Category category : categories) {
            result.add(new Category(category));
        }
        return result;
    }

    private int getPixel(float dp, View dragHandle) {
        return Math.round(dp * dragHandle.getResources().getDisplayMetrics().density);
    }

    private static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView title;
        public View dragHandle;

        public CategoryViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkbox);
            title = view.findViewById(R.id.title);
            dragHandle = view.findViewById(R.id.drag_handle);
        }
    }
}

