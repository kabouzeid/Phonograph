package com.kabouzeid.materialmusic.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.adapter.NavigationDrawerItemAdapter;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.model.NavigationDrawerItem;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.util.ArrayList;

public class NavigationDrawerFragment extends Fragment {
    public static final int NAVIGATION_DRAWER_HEADER = -1;
    private static final String TAG = NavigationDrawerFragment.class.getSimpleName();
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    public View fragmentRootView;
    private App app;
    private NavigationDrawerCallbacks callbacks;
    private NavigationDrawerItemAdapter drawerAdapter;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private View fragmentContainerView;

    private Button headerButton;
    private ImageView albumArt;
    private TextView songTitle;
    private TextView songArtist;

    private boolean fromSavedInstanceState;
    private boolean userLearnedDrawer;

    private int checkedPosition = 0;

    public NavigationDrawerFragment() {
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    public void setUp(int fragmentId, final DrawerLayout drawerLayout) {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);


        if (!userLearnedDrawer && !fromSavedInstanceState) {
            this.drawerLayout.openDrawer(fragmentContainerView);
            userLearnedDrawer = true;
            app.getDefaultSharedPreferences().edit().putBoolean(AppKeys.SP_USER_LEARNED_DRAWER, true).apply();
        }
    }

    public TextView getSongArtist() {
        return songArtist;
    }

    public ImageView getAlbumArtImageView() {
        return albumArt;
    }

    public TextView getSongTitle() {
        return songTitle;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        app = (App) getActivity().getApplicationContext();
        userLearnedDrawer = app.getDefaultSharedPreferences().getBoolean(AppKeys.SP_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            setItemChecked(savedInstanceState.getInt(STATE_SELECTED_POSITION));
            fromSavedInstanceState = true;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fragmentRootView = view;
        super.onViewCreated(view, savedInstanceState);
        initViews();
        setUpViews();
    }

    private void initViews() {
        drawerListView = (ListView) fragmentRootView.findViewById(R.id.navigation_drawer_list);
        final View drawerHeader = fragmentRootView.findViewById(R.id.header);
        headerButton = (Button) drawerHeader.findViewById(R.id.header_clickable);
        albumArt = (ImageView) drawerHeader.findViewById(R.id.album_art);
        songTitle = (TextView) drawerHeader.findViewById(R.id.song_title);
        songArtist = (TextView) drawerHeader.findViewById(R.id.song_artist);
    }

    private void setUpViews() {
        headerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(NAVIGATION_DRAWER_HEADER);
            }
        });
        setUpListView();
    }

    private void setUpListView() {
        final ArrayList<NavigationDrawerItem> navigationDrawerItems = new ArrayList<>();
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.songs), R.drawable.ic_my_library_music_white_24dp));
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.albums), R.drawable.ic_album_white_24dp));
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.artists), R.drawable.ic_person_white_24dp));
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.genres), R.drawable.ic_my_library_music_white_24dp));
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.playlists), R.drawable.ic_queue_music_white_24dp));

        drawerAdapter = new NavigationDrawerItemAdapter(getActivity(), R.id.navigation_drawer, navigationDrawerItems);

        final AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(drawerAdapter);
        animationAdapter.setAbsListView(drawerListView);

        drawerListView.setAdapter(animationAdapter);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
    }

    private void selectItem(final int position) {
        if (position != NAVIGATION_DRAWER_HEADER) {
            setItemChecked(position);
            if (drawerLayout != null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawer(fragmentContainerView);
                    }
                }, 400);
            }

        }
        if (callbacks != null) {
            callbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, checkedPosition);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    public void setItemChecked(final int position) {
        if (drawerAdapter != null) {
            drawerAdapter.setChecked(position);
            checkedPosition = position;
        }
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }
}
