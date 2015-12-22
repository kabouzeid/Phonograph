package com.kabouzeid.gramophone.ui.fragments.player;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.AlbumCoverPagerAdapter;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.ui.fragments.AbsMusicServiceFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayerAlbumCoverFragment extends AbsMusicServiceFragment implements ViewPager.OnPageChangeListener {

    @Bind(R.id.player_album_cover_viewpager)
    ViewPager viewPager;

    private AlbumCoverPagerAdapter pagerAdapter;
    private OnColorChangedListener onColorChangedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_album_cover, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager.setOffscreenPageLimit(2);
        updatePlayingQueue();
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.removeOnPageChangeListener(this);
        ButterKnife.unbind(this);
    }

    @Override
    public void onPlayingMetaChanged() {
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onQueueChanged() {
        updatePlayingQueue();
    }

    private void updatePlayingQueue() {
        viewPager.setAdapter(new AlbumCoverPagerAdapter(getFragmentManager(), MusicPlayerRemote.getPlayingQueue()));
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position != MusicPlayerRemote.getPosition()) {
            MusicPlayerRemote.playSongAt(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private static class ColorHolder {
        @ColorInt
        public int color;
    }

    public void showHeartAnimation() {

    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        onColorChangedListener = listener;
    }

    interface OnColorChangedListener {
        void onColorChanged(int color);
    }
}
