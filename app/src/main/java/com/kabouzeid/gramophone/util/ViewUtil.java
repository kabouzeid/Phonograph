package com.kabouzeid.gramophone.util;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.internal.view.menu.ListMenuItemView;
import android.support.v7.internal.view.menu.MenuPopupHelper;
import android.support.v7.widget.ActionMenuPresenter;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.gramophone.R;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ViewUtil {

    public static Animator createBackgroundColorTransition(final View v, final int startColor, final int endColor) {
        return createColorAnimator(v, "backgroundColor", startColor, endColor);
    }

    public static Animator createTextColorTransition(final TextView v, final int startColor, final int endColor) {
        return createColorAnimator(v, "textColor", startColor, endColor);
    }

    public final static int DEFAULT_COLOR_ANIMATION_DURATION = 500;

    private static Animator createColorAnimator(Object target, String propertyName, int startColor, int endColor) {
        ObjectAnimator animator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator = ObjectAnimator.ofArgb(target, propertyName, startColor, endColor);
        } else {
            animator = ObjectAnimator.ofInt(target, propertyName, startColor, endColor);
            animator.setEvaluator(new ArgbEvaluator());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
        }
        animator.setDuration(DEFAULT_COLOR_ANIMATION_DURATION);
        return animator;
    }

    /**
     * Should be called in {@link android.app.Activity#onPrepareOptionsMenu(Menu)} and {@link android.app.Activity#onOptionsItemSelected(MenuItem)}
     *
     * @param toolbar the toolbar to apply the tint on
     */
    public static void invalidateToolbarPopupMenuTint(@Nullable final Toolbar toolbar) {
        if (toolbar == null) return;
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Field f1 = Toolbar.class.getDeclaredField("mMenuView");
                    f1.setAccessible(true);
                    ActionMenuView actionMenuView = (ActionMenuView) f1.get(toolbar);

                    Field f2 = ActionMenuView.class.getDeclaredField("mPresenter");
                    f2.setAccessible(true);
                    ActionMenuPresenter presenter = (ActionMenuPresenter) f2.get(actionMenuView);

                    Field f3 = presenter.getClass().getDeclaredField("mOverflowPopup");
                    f3.setAccessible(true);
                    MenuPopupHelper overflowMenuPopupHelper = (MenuPopupHelper) f3.get(presenter);
                    ViewUtil.setTintForMenuPopupHelper(overflowMenuPopupHelper);

                    Field f4 = presenter.getClass().getDeclaredField("mActionButtonPopup");
                    f4.setAccessible(true);
                    MenuPopupHelper subMenuPopupHelper = (MenuPopupHelper) f4.get(presenter);
                    ViewUtil.setTintForMenuPopupHelper(subMenuPopupHelper);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void setTintForMenuPopupHelper(@Nullable MenuPopupHelper menuPopupHelper) {
        if (menuPopupHelper == null) return;
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
                            MDTintHelper.setTint(check, ThemeSingleton.get().positiveColor.getDefaultColor());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                check.setBackground(null);
                            }
                        }

                        RadioButton radioButton = (RadioButton) radioButtonField.get(iv);
                        if (radioButton != null) {
                            MDTintHelper.setTint(radioButton, ThemeSingleton.get().positiveColor.getDefaultColor());
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

    public static void setToolbarContentColorForBackground(@NonNull Context context, @Nullable Toolbar toolbar, @ColorInt final int backgroundColor) {
        ViewUtil.setToolbarContentDark(context, toolbar, ColorUtil.useDarkTextColorOnBackground(backgroundColor));
    }

    public static void setToolbarContentDark(@NonNull Context context, @Nullable Toolbar toolbar, boolean dark) {
        if (toolbar == null) return;

        toolbar.setTitleTextColor(ColorUtil.getPrimaryTextColor(context, dark));
        toolbar.setSubtitleTextColor(ColorUtil.getSecondaryTextColor(context, dark));

        setToolbarIconColor(context, toolbar, getToolbarIconColor(context, dark));
    }

    @ColorInt
    public static int getToolbarIconColor(Context context, boolean dark) {
        if (dark) {
            return ColorUtil.getSecondaryTextColor(context, true);
        } else {
            return ColorUtil.getPrimaryTextColor(context, false);
        }
    }

    public static void setToolbarIconColor(@NonNull final Context context, @Nullable final Toolbar toolbar, @ColorInt final int color) {
        if (toolbar == null) return;
        toolbar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                toolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final ArrayList<View> outViews = new ArrayList<>();
                final String overflowDescription = context.getString(R.string.abc_action_menu_overflow_description);
                toolbar.findViewsWithText(outViews, overflowDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (!outViews.isEmpty()) {
                    try {
                        ImageView overflowButton = (ImageView) outViews.get(0);
                        overflowButton.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    } catch (ClassCastException e) {
                        Log.e("setToolbarIconColor", "overflow button is not an ImageView", e);
                    }
                }

                Menu menu = toolbar.getMenu();
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    if (item.getIcon() != null) {
                        item.getIcon().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    }
                }

                Drawable navigationIcon = toolbar.getNavigationIcon();
                if (navigationIcon != null) {
                    navigationIcon = navigationIcon.mutate();
                    navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    toolbar.setNavigationIcon(navigationIcon);
                }
            }
        });
    }
}