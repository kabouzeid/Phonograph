package com.kabouzeid.materialmusic.ui.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.interfaces.KabViewsDisableAble;
import com.kabouzeid.materialmusic.interfaces.OnMusicRemoteEventListener;
import com.kabouzeid.materialmusic.lastfm.artist.LastFMArtistImageLoader;
import com.kabouzeid.materialmusic.loader.ArtistLoader;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.model.Artist;
import com.kabouzeid.materialmusic.ui.activities.base.AbsFabActivity;
import com.kabouzeid.materialmusic.ui.fragments.artistviewpager.AbsViewPagerTabArtistListFragment;
import com.kabouzeid.materialmusic.ui.fragments.artistviewpager.ViewPagerTabArtistAlbumFragment;
import com.kabouzeid.materialmusic.ui.fragments.artistviewpager.ViewPagerTabArtistBioFragment;
import com.kabouzeid.materialmusic.ui.fragments.artistviewpager.ViewPagerTabArtistSongListFragment;
import com.kabouzeid.materialmusic.util.Util;
import com.kabouzeid.materialmusic.util.ViewUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/*
*
* A lot of hackery is done in this activity. Changing things may will brake the whole activity.
*
* Should be kinda stable ONLY AS IT IS!!!
*
* */

public class ArtistDetailActivity extends AbsFabActivity implements OnMusicRemoteEventListener, KabViewsDisableAble, ObservableScrollViewCallbacks {
    public static final String TAG = ArtistDetailActivity.class.getSimpleName();

    public static final String ARG_ARTIST_ID = "com.kabouzeid.materialmusic.artist.id";
    public static final String ARG_ARTIST_NAME = "com.kabouzeid.materialmusic.artist.name";

    private static final boolean TOOLBAR_IS_STICKY = true;

    private boolean isAnimating;

    private Artist artist;

    private SlidingTabLayout slidingTabs;
    private View statusBar;
    private ImageView artistImageView;
    private View artistArtOverlayView;
    private View absAlbumListBackgroundView;
    private TextView artistTitleText;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private NavigationAdapter navigationAdapter;
    private int toolbarHeight;
    private int headerOffset;
    private int titleViewHeight;
    private int artistImageViewHeight;
    private int toolbarColor;
    private int tabHeight;

    private Bitmap artistImage;

    private Fragment currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpTranslucence(true, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        getIntentExtras();
        initViews();
        setUpObservableListViewParams();
        setUpToolBar();
        setUpViews();
        lollipopTransitionImageWrongSizeFix();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_current_playing:
                openCurrentPlayingIfPossible(null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        artistImageView = (ImageView) findViewById(R.id.artist_image);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        artistArtOverlayView = findViewById(R.id.overlay);
        artistTitleText = (TextView) findViewById(R.id.artist_name);
        absAlbumListBackgroundView = findViewById(R.id.list_background);
        statusBar = findViewById(R.id.statusBar);
        slidingTabs = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
    }

    private void setUpObservableListViewParams() {
        artistImageViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        toolbarColor = Util.resolveColor(this, R.attr.colorPrimary);
        toolbarHeight = Util.getActionBarSize(this);
        titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        headerOffset = toolbarHeight;
        headerOffset += getResources().getDimensionPixelSize(R.dimen.statusMargin);
        tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);
    }

    private void setUpViews() {
        artistTitleText.setText(artist.name);
        ViewHelper.setAlpha(artistArtOverlayView, 0);

        setUpArtistImageAndApplyPalette();
        setUpViewPatch();
        setUpSlidingTabs();
    }

    private void setUpSlidingTabs() {
        navigationAdapter = new NavigationAdapter(this, artist);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(navigationAdapter);
        viewPager.setCurrentItem(1);

        slidingTabs.setViewPager(viewPager);
        slidingTabs.setDistributeEvenly(true);
        slidingTabs.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabs.setSelectedIndicatorColors(Util.resolveColor(this, R.attr.colorAccent));
        slidingTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentFragment = navigationAdapter.getItemAt(position);
                if (currentFragment instanceof AbsViewPagerTabArtistListFragment) {
                    restoreY(((AbsViewPagerTabArtistListFragment) currentFragment).getY());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setUpArtistImageAndApplyPalette() {
        if (artistImage == null) {
            LastFMArtistImageLoader.loadArtistImage(this, artist.name, new LastFMArtistImageLoader.ArtistImageLoaderCallback() {
                @Override
                public void onArtistImageLoaded(Bitmap artistImage) {
                    if (artistImage != null) {
                        ArtistDetailActivity.this.artistImage = artistImage;
                        artistImageView.setImageBitmap(artistImage);
                        applyPalette(artistImage);
                    }
                }
            });
        } else {
            artistImageView.setImageBitmap(artistImage);
            applyPalette(artistImage);
        }
    }

    private void setUpViewPatch() {
        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                absAlbumListBackgroundView.getLayoutParams().height = contentView.getHeight();
            }
        });
    }

    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (!TOOLBAR_IS_STICKY) {
            toolbar.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void applyPalette(Bitmap bitmap) {
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch != null) {
                    toolbarColor = swatch.getRgb();
                    artistArtOverlayView.setBackgroundColor(swatch.getRgb());
                    artistTitleText.setBackgroundColor(swatch.getRgb());
                    slidingTabs.setBackgroundColor(swatch.getRgb());
                    artistTitleText.setTextColor(swatch.getTitleTextColor());
                }
            }
        });
    }

    @Override
    public void enableViews() {
        super.enableViews();
        viewPager.setEnabled(true);
        toolbar.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        viewPager.setEnabled(false);
        toolbar.setEnabled(false);
    }

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        final int artistId = intentExtras.getInt(AppKeys.E_ARTIST);
        artist = ArtistLoader.getArtist(this, artistId);
        if (artist == null) {
            finish();
        }
    }

    @Override
    public void onScrollChanged(int scrollY, boolean b, boolean b2) {
        if (!isAnimating) {
            int titleTranslationY = getTitleTranslation(scrollY);
            ViewHelper.setTranslationY(artistArtOverlayView, getOverlayTranslation(scrollY));
            ViewHelper.setTranslationY(artistImageView, getImageViewTranslation(scrollY));
            ViewHelper.setTranslationY(absAlbumListBackgroundView, getListBackgroundTranslation(scrollY));
            ViewHelper.setAlpha(artistArtOverlayView, getOverlayAlpha(scrollY));
            ViewHelper.setTranslationY(artistTitleText, titleTranslationY);
            ViewHelper.setTranslationY(slidingTabs, titleTranslationY);
            ViewHelper.setTranslationY(getFab(), getFabTranslation(scrollY));
            translateToolBar(scrollY);
        }
    }

    private int getImageViewTranslation(int scrollY) {
        int minOverlayTransitionY = headerOffset - artistArtOverlayView.getHeight();
        return Math.max(minOverlayTransitionY, Math.min(0, -scrollY / 2));
    }

    private int getOverlayTranslation(int scrollY) {
        int minOverlayTransitionY = headerOffset - artistArtOverlayView.getHeight();
        return Math.max(minOverlayTransitionY, Math.min(0, -scrollY));
    }

    private int getListBackgroundTranslation(int scrollY) {
        return Math.max(0, -scrollY + artistImageViewHeight);
    }

    private int getTitleTranslation(int scrollY) {
        int maxTitleTranslationY = artistImageViewHeight;
        int titleTranslationY = maxTitleTranslationY - scrollY;
        if (TOOLBAR_IS_STICKY) {
            titleTranslationY = Math.max(headerOffset, titleTranslationY);
        }
        return titleTranslationY;
    }

    private int getFabTranslation(int scrollY) {
        return getTitleTranslation(scrollY) + titleViewHeight + tabHeight - (getFab().getHeight() / 2);
    }

    private float getOverlayAlpha(int scrollY) {
        float flexibleRange = artistImageViewHeight - headerOffset;
        return Math.max(0, Math.min(1, (float) scrollY / flexibleRange));
    }

    private void translateToolBar(int scrollY) {
        if (TOOLBAR_IS_STICKY) {
            // Change alpha of toolbar background
            if (-scrollY + artistImageViewHeight <= headerOffset) {
                ViewUtil.setBackgroundAlpha(toolbar, 1, toolbarColor);
                ViewUtil.setBackgroundAlpha(statusBar, 1, toolbarColor);

            } else {
                ViewUtil.setBackgroundAlpha(toolbar, 0, toolbarColor);
                ViewUtil.setBackgroundAlpha(statusBar, 0, toolbarColor);
            }
        } else {
            // Translate Toolbar
            if (scrollY < artistImageViewHeight) {
                ViewHelper.setTranslationY(toolbar, 0);
            } else {
                ViewHelper.setTranslationY(toolbar, -scrollY);
            }
        }
    }

    public void restoreY(final int scrollY) {
        translateToolBar(scrollY);
        int animationTime = 1000;
        DecelerateInterpolator interpolator = new DecelerateInterpolator(4);
        int titleTranslationY = getTitleTranslation(scrollY);
        ViewPropertyAnimator.animate(artistArtOverlayView).y(getOverlayTranslation(scrollY)).setDuration(animationTime).setInterpolator(interpolator).start();
        ViewPropertyAnimator.animate(artistImageView).y(getImageViewTranslation(scrollY)).setDuration(animationTime).setInterpolator(interpolator).start();
        ViewPropertyAnimator.animate(absAlbumListBackgroundView).y(getListBackgroundTranslation(scrollY)).setDuration(animationTime).setInterpolator(interpolator).start();
        ViewPropertyAnimator.animate(artistArtOverlayView).alpha(getOverlayAlpha(scrollY)).setDuration(animationTime).setInterpolator(interpolator).start();
        ViewPropertyAnimator.animate(slidingTabs).y(titleTranslationY + titleViewHeight).setDuration(animationTime).setInterpolator(interpolator).start();
        ViewPropertyAnimator.animate(artistTitleText).y(titleTranslationY).setDuration(animationTime).setInterpolator(interpolator).start();
        ViewPropertyAnimator.animate(getFab()).y(getFabTranslation(scrollY)).setDuration(animationTime).setInterpolator(interpolator).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                translateToolBar(scrollY);
                isAnimating = false;
                if (currentFragment instanceof AbsViewPagerTabArtistListFragment) {
                    onScrollChanged((((AbsViewPagerTabArtistListFragment) currentFragment).getY()), false, false);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                isAnimating = true;
            }
        }).start();
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    @Override
    public void goToArtist(int artistId) {
        if (artist.id != artistId) {
            super.goToArtist(artistId);
        }
    }

    private void lollipopTransitionImageWrongSizeFix() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    if (artistImage == null) {
                        LastFMArtistImageLoader.loadArtistImage(ArtistDetailActivity.this, artist.name, new LastFMArtistImageLoader.ArtistImageLoaderCallback() {
                            @Override
                            public void onArtistImageLoaded(Bitmap artistImage) {
                                if (artistImage != null) {
                                    artistImageView.setImageBitmap(artistImage);
                                }
                            }
                        });
                    } else {
                        artistImageView.setImageBitmap(artistImage);
                    }
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
        }
    }

    private static class NavigationAdapter extends FragmentPagerAdapter {

        private String[] titles;

        private SparseArray<Fragment> mPages;
        private Artist artist;
        private Context context;

        public NavigationAdapter(Activity activity, Artist artist) {
            super(activity.getFragmentManager());
            this.artist = artist;
            mPages = new SparseArray<>();
            context = activity;
            titles = new String[]{
                    context.getResources().getString(R.string.tab_songs),
                    context.getResources().getString(R.string.tab_albums),
                    context.getResources().getString(R.string.tab_biography)
            };
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putInt(ARG_ARTIST_ID, artist.id);
            args.putString(ARG_ARTIST_NAME, artist.name);
            Fragment f;
            switch (position) {
                case 1:
                    f = mPages.get(position, new ViewPagerTabArtistAlbumFragment());
                    break;
                case 0:
                    f = mPages.get(position, new ViewPagerTabArtistSongListFragment());
                    break;
                case 2:
                    f = mPages.get(position, new ViewPagerTabArtistBioFragment());
                    break;
                default:
                    f = mPages.get(position, new MainActivity.PlaceholderFragment());
                    break;
            }
            f.setArguments(args);
            mPages.put(position, f);
            return f;
        }

        public Fragment getItemAt(int position) {
            return mPages.get(position, null);
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (0 <= mPages.indexOfKey(position)) {
                mPages.remove(position);
            }
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
