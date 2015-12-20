package com.kabouzeid.gramophone.ui.fragments.player;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.MusicServiceEventListener;
import com.kabouzeid.gramophone.misc.SimpleAnimatorListener;
import com.kabouzeid.gramophone.ui.activities.base.AbsMusicServiceActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayerAlbumCoverFragment extends Fragment implements MusicServiceEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Bind(R.id.player_image)
    ImageView albumCover;
    @Bind(R.id.player_favorite_icon)
    ImageView favoriteIcon;

    private AbsMusicServiceActivity activity;
    private OnColorChangedListener onColorChangedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activity = (AbsMusicServiceActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + AbsMusicServiceActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_album_cover, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        forceSquareAlbumCover(PreferenceUtil.getInstance(getContext()).forceSquareAlbumCover());

        PreferenceUtil.getInstance(getContext()).registerOnSharedPreferenceChangedListener(this);
        activity.addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.removeMusicServiceEventListener(this);
        PreferenceUtil.getInstance(activity).unregisterOnSharedPreferenceChangedListener(this);
        ButterKnife.unbind(this);
    }

    @Override
    public void onPlayingMetaChanged() {
        loadAlbumCover();
    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayStateChanged() {

    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.FORCE_SQUARE_ALBUM_COVER:
                forceSquareAlbumCover(PreferenceUtil.getInstance(activity).forceSquareAlbumCover());
                break;
        }
    }

    private static class ColorHolder {
        @ColorInt
        public int color;
    }

    private void loadAlbumCover() {
        final ColorHolder colorHolder = new ColorHolder();
        ImageLoader.getInstance().displayImage(
                MusicUtil.getSongImageLoaderString(MusicPlayerRemote.getCurrentSong()),
                albumCover,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .postProcessor(new BitmapProcessor() {
                            @Override
                            public Bitmap process(Bitmap bitmap) {
                                colorHolder.color = ColorUtil.generateColor(activity, bitmap);
                                return bitmap;
                            }
                        })
                        .displayer(new FadeInBitmapDisplayer(ViewUtil.DEFAULT_COLOR_ANIMATION_DURATION))
                        .build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, @Nullable FailReason failReason) {
                        FadeInBitmapDisplayer.animate(view, ViewUtil.DEFAULT_COLOR_ANIMATION_DURATION);
                        setColor(ColorUtil.resolveColor(activity, R.attr.default_bar_color));
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, @Nullable Bitmap loadedImage) {
                        if (loadedImage == null) {
                            onLoadingFailed(imageUri, view, null);
                            return;
                        }
                        setColor(colorHolder.color);
                    }
                }
        );
    }

    public void showHeartAnimation() {
        favoriteIcon.clearAnimation();

        favoriteIcon.setAlpha(0f);
        favoriteIcon.setScaleX(0f);
        favoriteIcon.setScaleY(0f);
        favoriteIcon.setVisibility(View.VISIBLE);
        favoriteIcon.setPivotX(favoriteIcon.getWidth() / 2);
        favoriteIcon.setPivotY(favoriteIcon.getHeight() / 2);

        favoriteIcon.animate()
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        favoriteIcon.setVisibility(View.INVISIBLE);
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        favoriteIcon.animate()
                                .setDuration(500)
                                .setInterpolator(new AccelerateInterpolator())
                                .scaleX(0f)
                                .scaleY(0f)
                                .alpha(0f)
                                .start();
                    }
                })
                .start();
    }

    public void forceSquareAlbumCover(boolean forceSquareAlbumCover) {
        albumCover.setScaleType(forceSquareAlbumCover ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
    }

    private void setColor(int color) {
        if (onColorChangedListener != null) onColorChangedListener.onColorChanged(color);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        onColorChangedListener = listener;
    }

    interface OnColorChangedListener {
        void onColorChanged(int color);
    }
}
