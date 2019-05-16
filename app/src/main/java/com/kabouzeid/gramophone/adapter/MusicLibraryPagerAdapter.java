package com.kabouzeid.gramophone.adapter;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.model.CategoryInfo;
import com.kabouzeid.gramophone.ui.fragments.mainactivity.library.pager.AlbumsFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivity.library.pager.ArtistsFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivity.library.pager.GenresFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivity.library.pager.PlaylistsFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivity.library.pager.SongsFragment;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MusicLibraryPagerAdapter extends FragmentPagerAdapter {

    private final SparseArray<WeakReference<Fragment>> mFragmentArray = new SparseArray<>();

    private final List<Holder> mHolderList = new ArrayList<>();

    @NonNull
    private final Context mContext;

    public MusicLibraryPagerAdapter(@NonNull final Context context, final FragmentManager fragmentManager) {
        super(fragmentManager);
        mContext = context;
        setCategoryInfos(PreferenceUtil.getInstance(context).getLibraryCategoryInfos());
    }

    public void setCategoryInfos(@NonNull List<CategoryInfo> categoryInfos) {
        mHolderList.clear();

        for (CategoryInfo categoryInfo : categoryInfos) {
            if (categoryInfo.visible) {
                MusicFragments fragment = MusicFragments.valueOf(categoryInfo.category.toString());
                Holder holder = new Holder();
                holder.mClassName = fragment.getFragmentClass().getName();
                holder.title = mContext.getResources()
                        .getString(categoryInfo.category.stringRes)
                        .toUpperCase(Locale.getDefault());
                mHolderList.add(holder);
            }
        }

        alignCache();
        notifyDataSetChanged();
    }

    public Fragment getFragment(final int position) {
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null && mWeakFragment.get() != null) {
            return mWeakFragment.get();
        }
        return getItem(position);
    }

    @Override
    public int getItemPosition(@NonNull Object fragment) {
        for (int i = 0, size = mHolderList.size(); i < size; i++) {
            Holder holder = mHolderList.get(i);
            if (holder.mClassName.equals(fragment.getClass().getName())) {
                return i;
            }
        }
        return POSITION_NONE;
    }

    @Override
    public long getItemId(int position) {
        // as fragment position is not fixed, we can't use position as id
        return MusicFragments.of(getFragment(position).getClass()).ordinal();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final Fragment mFragment = (Fragment) super.instantiateItem(container, position);
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null) {
            mWeakFragment.clear();
        }
        mFragmentArray.put(position, new WeakReference<>(mFragment));
        return mFragment;
    }

    @Override
    public Fragment getItem(final int position) {
        final Holder mCurrentHolder = mHolderList.get(position);
        return Fragment.instantiate(mContext,
                mCurrentHolder.mClassName, mCurrentHolder.mParams);
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        super.destroyItem(container, position, object);
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null) {
            mWeakFragment.clear();
        }
    }

    @Override
    public int getCount() {
        return mHolderList.size();
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(final int position) {
        return mHolderList.get(position).title;
    }

    /**
     * Aligns the fragment cache with the current category layout.
     */
    private void alignCache() {
        if (mFragmentArray.size() == 0) return;

        HashMap<String, WeakReference<Fragment>> mappings = new HashMap<>(mFragmentArray.size());

        for (int i = 0, size = mFragmentArray.size(); i < size; i++) {
            WeakReference<Fragment> ref = mFragmentArray.valueAt(i);
            Fragment fragment = ref.get();
            if (fragment != null) {
                mappings.put(fragment.getClass().getName(), ref);
            }
        }
        for (int i = 0, size = mHolderList.size(); i < size; i++) {
            WeakReference<Fragment> ref = mappings.get(mHolderList.get(i).mClassName);
            if (ref != null) {
                mFragmentArray.put(i, ref);
            } else {
                mFragmentArray.remove(i);
            }
        }
    }

    public enum MusicFragments {
        SONGS(SongsFragment.class),
        ALBUMS(AlbumsFragment.class),
        ARTISTS(ArtistsFragment.class),
        GENRES(GenresFragment.class),
        PLAYLISTS(PlaylistsFragment.class);

        private final Class<? extends Fragment> mFragmentClass;

        MusicFragments(final Class<? extends Fragment> fragmentClass) {
            mFragmentClass = fragmentClass;
        }

        public Class<? extends Fragment> getFragmentClass() {
            return mFragmentClass;
        }

        public static MusicFragments of(Class<?> cl) {
            MusicFragments[] fragments = All.FRAGMENTS;
            for (MusicFragments fragment : fragments) {
                if (cl.equals(fragment.mFragmentClass))
                    return fragment;
            }

            throw new IllegalArgumentException("Unknown music fragment " + cl);
        }

        private static class All {
            public static final MusicFragments[] FRAGMENTS = values();
        }
    }

    private final static class Holder {
        String mClassName;
        Bundle mParams;
        String title;
    }
}
