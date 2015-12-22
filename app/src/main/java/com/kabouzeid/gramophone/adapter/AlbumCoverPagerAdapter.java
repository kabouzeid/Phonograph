package com.kabouzeid.gramophone.adapter;

import android.animation.Animator;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.misc.SimpleAnimatorListener;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumCoverPagerAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = AlbumCoverPagerAdapter.class.getSimpleName();

    private ArrayList<Song> dataSet;

    public AlbumCoverPagerAdapter(FragmentManager fm, ArrayList<Song> dataSet) {
        super(fm);
        this.dataSet = dataSet;
    }

    @Override
    public Fragment getItem(final int position) {
        return AlbumCoverFragment.newInstance(dataSet.get(position));
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    public static class AlbumCoverFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private static final String SONG_ARG = "song";

        @Bind(R.id.player_image)
        ImageView albumCover;
        @Bind(R.id.player_favorite_icon)
        ImageView favoriteIcon;

        private int color;
        private Song song;

        public static AlbumCoverFragment newInstance(final Song song) {
            AlbumCoverFragment frag = new AlbumCoverFragment();
            final Bundle args = new Bundle();
            args.putSerializable(SONG_ARG, song);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            song = (Song) getArguments().getSerializable(SONG_ARG);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_album_cover, container, false);
            ButterKnife.bind(this, view);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            forceSquareAlbumCover(PreferenceUtil.getInstance(getContext()).forceSquareAlbumCover());
            PreferenceUtil.getInstance(getActivity()).registerOnSharedPreferenceChangedListener(this);
            loadAlbumCover();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            PreferenceUtil.getInstance(getActivity()).unregisterOnSharedPreferenceChangedListener(this);
            ButterKnife.unbind(this);
        }

        private void loadAlbumCover() {
            ImageLoader.getInstance().displayImage(
                    MusicUtil.getSongImageLoaderString(song),
                    albumCover,
                    new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .showImageOnFail(R.drawable.default_album_art)
                            .postProcessor(new BitmapProcessor() {
                                @Override
                                public Bitmap process(Bitmap bitmap) {
                                    color = ColorUtil.generateColor(getActivity(), bitmap);
                                    return bitmap;
                                }
                            })
                            .build(),
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingFailed(String imageUri, View view, @Nullable FailReason failReason) {
                            color = ColorUtil.resolveColor(getActivity(), R.attr.default_bar_color);
                            notifyColorIsReady();
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, @Nullable Bitmap loadedImage) {
                            if (loadedImage == null) {
                                onLoadingFailed(imageUri, view, null);
                                return;
                            }
                            notifyColorIsReady();
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
                    .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME / 2)
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
                                    .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME / 2)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .scaleX(0f)
                                    .scaleY(0f)
                                    .alpha(0f)
                                    .start();
                        }
                    })
                    .start();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PreferenceUtil.FORCE_SQUARE_ALBUM_COVER:
                    forceSquareAlbumCover(PreferenceUtil.getInstance(getActivity()).forceSquareAlbumCover());
                    break;
            }
        }

        public void forceSquareAlbumCover(boolean forceSquareAlbumCover) {
            albumCover.setScaleType(forceSquareAlbumCover ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        }

        private void notifyColorIsReady() {
            // TODO
        }
    }
}

