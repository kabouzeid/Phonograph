package com.kabouzeid.gramophone.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.animation.DecelerateInterpolator;

import com.kabouzeid.gramophone.R;

public class PlayPauseDrawable extends Drawable {
    private static final long PLAY_PAUSE_ANIMATION_DURATION = 250;

    private static final Property<PlayPauseDrawable, Float> PROGRESS =
            new Property<PlayPauseDrawable, Float>(Float.class, "progress") {
                @Override
                public Float get(PlayPauseDrawable d) {
                    return d.getProgress();
                }

                @Override
                public void set(PlayPauseDrawable d, Float value) {
                    d.setProgress(value);
                }
            };

    private final Path leftPauseBar = new Path();
    private final Path rightPauseBar = new Path();
    private final Paint paint = new Paint();
    private final float pauseBarWidth;
    private final float pauseBarHeight;
    private final float pauseBarDistance;

    private float width;
    private float height;
    private final float fallBackWidth;
    private final float fallBackHeight;

    private float progress;
    private boolean isPlay;
    private boolean isPlaySet;

    private AnimatorSet animatorSet;

    public PlayPauseDrawable(Context context) {
        final Resources res = context.getResources();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        pauseBarWidth = res.getDimensionPixelSize(R.dimen.pause_bar_width);
        pauseBarHeight = res.getDimensionPixelSize(R.dimen.pause_bar_height);
        pauseBarDistance = res.getDimensionPixelSize(R.dimen.pause_bar_distance);
        fallBackWidth = res.getDimensionPixelSize(R.dimen.fab_icon_bound_width);
        fallBackHeight = res.getDimensionPixelSize(R.dimen.fab_icon_bound_height);
    }

    @Override
    protected void onBoundsChange(final Rect bounds) {
        super.onBoundsChange(bounds);
        if (bounds.width() > 0 && bounds.height() > 0) {
            width = bounds.width();
            height = bounds.height();
        } else {
            width = fallBackWidth;
            height = fallBackHeight;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        leftPauseBar.rewind();
        rightPauseBar.rewind();

        // The current distance between the two pause bars.
        final float barDist = lerp(pauseBarDistance, 0, progress);
        // The current width of each pause bar.
        final float barWidth = lerp(pauseBarWidth, pauseBarHeight / 1.75f, progress);
        // The current position of the left pause bar's top left coordinate.
        final float firstBarTopLeft = lerp(0, barWidth, progress);
        // The current position of the right pause bar's top right coordinate.
        final float secondBarTopRight = lerp(2 * barWidth + barDist, barWidth + barDist, progress);

        // Draw the left pause bar. The left pause bar transforms into the
        // top half of the play button triangle by animating the position of the
        // rectangle's top left coordinate and expanding its bottom width.
        leftPauseBar.moveTo(0, 0);
        leftPauseBar.lineTo(firstBarTopLeft, -pauseBarHeight);
        leftPauseBar.lineTo(barWidth, -pauseBarHeight);
        leftPauseBar.lineTo(barWidth, 0);
        leftPauseBar.close();

        // Draw the right pause bar. The right pause bar transforms into the
        // bottom half of the play button triangle by animating the position of the
        // rectangle's top right coordinate and expanding its bottom width.
        rightPauseBar.moveTo(barWidth + barDist, 0);
        rightPauseBar.lineTo(barWidth + barDist, -pauseBarHeight);
        rightPauseBar.lineTo(secondBarTopRight, -pauseBarHeight);
        rightPauseBar.lineTo(2 * barWidth + barDist, 0);
        rightPauseBar.close();

        canvas.save();

        // Translate the play button a tiny bit to the right so it looks more centered.
        canvas.translate(lerp(0, pauseBarHeight / 8f, progress), 0);

        // (1) Pause --> Play: rotate 0 to 90 degrees clockwise.
        // (2) Play --> Pause: rotate 90 to 180 degrees clockwise.
        final float rotationProgress = isPlay ? 1 - progress : progress;
        final float startingRotation = isPlay ? 90 : 0;
        canvas.rotate(lerp(startingRotation, startingRotation + 90, rotationProgress), width / 2f, height / 2f);

        // Position the pause/play button in the center of the drawable's bounds.
        canvas.translate(width / 2f - ((2 * barWidth + barDist) / 2f), height / 2f + (pauseBarHeight / 2f));

        // Draw the two bars that form the animated pause/play button.
        canvas.drawPath(leftPauseBar, paint);
        canvas.drawPath(rightPauseBar, paint);

        canvas.restore();
    }

    private Animator getPausePlayAnimator() {
        isPlaySet = !isPlaySet;
        final Animator anim = ObjectAnimator.ofFloat(this, PROGRESS, isPlay ? 1 : 0, isPlay ? 0 : 1);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isPlay = !isPlay;
            }
        });
        return anim;
    }

    public boolean isPlay() {
        return isPlaySet;
    }

    private void setProgress(float progress) {
        this.progress = progress;
        invalidateSelf();
    }

    private float getProgress() {
        return progress;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * Linear interpolate between a and b with parameter t.
     */
    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public void animatedPlay() {
        if (!isPlaySet) {
            togglePlayPause();
        }
    }

    public void animatedPause() {
        if (isPlaySet) {
            togglePlayPause();
        }
    }

    public void setPlay() {
        isPlaySet = true;
        isPlay = true;
        setProgress(1);
    }

    public void setPause() {
        isPlaySet = false;
        isPlay = false;
        setProgress(0);
    }

    public void togglePlayPause() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }

        animatorSet = new AnimatorSet();
        final Animator pausePlayAnim = getPausePlayAnimator();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
        animatorSet.playTogether(pausePlayAnim);
        animatorSet.start();
    }
}
