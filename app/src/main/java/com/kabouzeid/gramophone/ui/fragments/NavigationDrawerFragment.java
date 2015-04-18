package com.kabouzeid.gramophone.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.NavigationDrawerItemAdapter;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.NavigationDrawerItem;

import java.util.ArrayList;

public class NavigationDrawerFragment extends Fragment {

    public static final int NAVIGATION_DRAWER_HEADER = -1;
    public static final int ABOUT_INDEX = 4;
    public static final int SETTINGS_INDEX = 5;
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    public View fragmentRootView;
    private NavigationDrawerCallbacks callbacks;
    private NavigationDrawerItemAdapter drawerAdapter;
    private DrawerLayout drawerLayout;
    private RecyclerView drawerRecyclerView;
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
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(AppKeys.SP_USER_LEARNED_DRAWER, true).apply();
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
        userLearnedDrawer = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(AppKeys.SP_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            setItemChecked(savedInstanceState.getInt(STATE_SELECTED_POSITION));
            fromSavedInstanceState = true;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        drawerRecyclerView = (RecyclerView) fragmentRootView.findViewById(R.id.navigation_drawer_list);
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
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.songs), R.drawable.ic_audiotrack_white_24dp));
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.albums), R.drawable.ic_album_white_24dp));
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.artists), R.drawable.ic_person_white_24dp));
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.playlists), R.drawable.ic_queue_music_white_24dp));
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.action_about), R.drawable.ic_help_white_24dp));
        navigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.action_settings), R.drawable.ic_settings_white_24dp));

        drawerAdapter = new NavigationDrawerItemAdapter(getActivity(), navigationDrawerItems, new NavigationDrawerItemAdapter.Callback() {
            @Override
            public void onItemSelected(int index) {
                selectItem(index);
            }
        });
        drawerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        drawerRecyclerView.setAdapter(drawerAdapter);
    }

    private void selectItem(final int position) {
        if (position != NAVIGATION_DRAWER_HEADER &&
                position != ABOUT_INDEX && position != SETTINGS_INDEX) {
            setItemChecked(position);
            if (drawerLayout != null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawers();
                    }
                }, 200);
            }
        }
        if (callbacks != null)
            callbacks.onNavigationDrawerItemSelected(position);
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

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }
}
