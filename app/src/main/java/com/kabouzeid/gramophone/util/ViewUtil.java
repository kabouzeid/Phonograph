package com.kabouzeid.gramophone.util;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.internal.view.menu.ListMenuItemView;
import android.support.v7.internal.view.menu.MenuPopupHelper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.internal.MDTintHelper;

import java.lang.reflect.Field;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ViewUtil {
    public final static int DEFAULT_COLOR_ANIMATION_DURATION = 500;

    public static void applyBackgroundColorFromBitmap(@Nullable Bitmap bitmap, final int defaultBgColor, @Nullable final View[] views, @Nullable final TextView[] textViews, final boolean animate) {
        if (bitmap != null) {
            Palette.from(bitmap)
                    .resizeBitmapSize(100)
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(@NonNull Palette palette) {
                            applyBackgroundColor(palette.getVibrantColor(defaultBgColor), views, textViews, animate);
                        }
                    });
        } else {
            applyBackgroundColor(defaultBgColor, views, textViews, animate);
        }
    }

    public static void applyBackgroundColor(int bgColor, @Nullable final View[] views, @Nullable TextView[] textViews, final boolean animate) {
        if (views != null) {
            for (View view : views) {
                if (view != null) {
                    if (animate) {
                        ViewUtil.animateViewColor(view, view.getDrawingCacheBackgroundColor(), bgColor);
                    } else {
                        view.setBackgroundColor(bgColor);
                    }
                }
            }
        }
        if (textViews != null) {
            int textColor = ColorUtil.getTextColorForBackground(bgColor);
            for (TextView textView : textViews) {
                if (textView != null) {
                    if (animate) {
                        animateTextColor(textView, textView.getCurrentTextColor(), textColor);
                    } else {
                        textView.setTextColor(textColor);
                    }
                }
            }
        }
    }

    public static void animateViewColor(final View v, final int startColor, final int endColor) {
        animateViewColor(v, startColor, endColor, DEFAULT_COLOR_ANIMATION_DURATION);
    }

    public static void animateViewColor(final View v, final int startColor, final int endColor, final int duration) {
        ObjectAnimator animator = ObjectAnimator.ofObject(v, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
        }
        animator.setDuration(duration);
        animator.start();
    }

    public static void animateTextColor(final TextView v, final int startColor, final int endColor) {
        animateTextColor(v, startColor, endColor, DEFAULT_COLOR_ANIMATION_DURATION);
    }

    public static void animateTextColor(final TextView v, final int startColor, final int endColor, final int duration) {
        ObjectAnimator animator = ObjectAnimator.ofObject(v, "textColor",
                new ArgbEvaluator(), startColor, endColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
        }
        animator.setDuration(duration);
        animator.start();
    }

    public static void setBackgroundAlpha(@NonNull View view, float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        view.setBackgroundColor(a + rgb);
    }

    public static void addOnGlobalLayoutListener(@NonNull final View view, @NonNull final Runnable runnable) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    //noinspection deprecation
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                runnable.run();
            }
        });
    }

    public static void setCheckBoxTintForMenu(@Nullable MenuPopupHelper menuPopupHelper) {
        if (menuPopupHelper != null) {
            final ListView listView = menuPopupHelper.getPopup().getListView();
            listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        Field checkboxField = ListMenuItemView.class.getDeclaredField("mCheckBox");
                        checkboxField.setAccessible(true);
                        Field radioButtonField = ListMenuItemView.class.getDeclaredField("mRadioButton");
                        radioButtonField.setAccessible(true);

                        for (int i = 0; i < listView.getChildCount(); i++) {
                            View v = listView.getChildAt(i);
                            if (!(v instanceof ListMenuItemView)) continue;
                            ListMenuItemView iv = (ListMenuItemView) v;

                            CheckBox check = (CheckBox) checkboxField.get(iv);
                            if (check != null) {
                                MDTintHelper.setTint(check, ThemeSingleton.get().positiveColor);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    check.setBackground(null);
                                }
                            }

                            RadioButton radioButton = (RadioButton) radioButtonField.get(iv);
                            if (radioButton != null) {
                                MDTintHelper.setTint(radioButton, ThemeSingleton.get().positiveColor);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    radioButton.setBackground(null);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        //noinspection deprecation
                        listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }
}