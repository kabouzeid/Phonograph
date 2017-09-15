package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kabouzeid.gramophone.util.ViewUtil;

public class HorizontalItemDivider extends RecyclerView.ItemDecoration {

    private Paint mPaint;
    private float mStartOffset;


    public HorizontalItemDivider(Context context, @ColorInt int lineColor, int startOffset) {
        this.mPaint = new Paint();
        this.mPaint.setColor(lineColor);
        this.mPaint.setStrokeWidth(ViewUtil.convertDpToPixel(1, context.getResources()));
        this.mStartOffset = ViewUtil.convertDpToPixel(startOffset, context.getResources());
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        float startX = parent.getPaddingLeft() + mStartOffset;
        float stopX = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        /* Draw lines only for visible items and skip the last item in the list from drawing the divider*/
        for(int i = 0; i < childCount - 1; i++){
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int y = child.getBottom() + params.bottomMargin;
            c.drawLine(startX,y,stopX,y,mPaint);
        }
    }
}
