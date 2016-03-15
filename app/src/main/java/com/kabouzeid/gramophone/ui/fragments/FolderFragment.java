package com.kabouzeid.gramophone.ui.fragments;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.SongFileAdapter;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.loader.SortedCursor;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.PhonographColorUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.views.BreadCrumbLayout;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

public class FolderFragment extends AbsMainActivityFragment implements MainActivity.MainActivityFragmentCallbacks, CabHolder, BreadCrumbLayout.SelectionCallback, SongFileAdapter.Callbacks, AppBarLayout.OnOffsetChangedListener {
    public static final String TAG = FolderFragment.class.getSimpleName();

    protected static final String PATH = "path";
    protected static final String CRUMBS = "crumbs";

    @Bind(R.id.container)
    View container;
    @Bind(android.R.id.empty)
    View empty;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.bread_crumbs)
    BreadCrumbLayout breadCrumbs;
    @Bind(R.id.appbar)
    AppBarLayout appbar;
    @Bind(R.id.recycler_view)
    FastScrollRecyclerView recyclerView;

    private SongFileAdapter adapter;
    private MaterialCab cab;

    public FolderFragment() {
    }

    public static FolderFragment newInstance() {
        return newInstance(getDefaultStartFolder());
    }

    public static FolderFragment newInstance(File directory) {
        FolderFragment frag = new FolderFragment();
        Bundle b = new Bundle();
        b.putSerializable(PATH, directory);
        frag.setArguments(b);
        return frag;
    }

    public void setCrumb(BreadCrumbLayout.Crumb crumb, boolean addToHistory) {
        saveScrollPosition();
        updateAdapter(crumb.getFile());
        recyclerView.getLayoutManager().scrollToPosition(crumb.getScrollPosition());
        breadCrumbs.setActiveOrAdd(crumb, false);
        if (addToHistory)
            breadCrumbs.addHistory(crumb);
    }

    private void saveScrollPosition() {
        if (breadCrumbs.size() > 0) {
            BreadCrumbLayout.Crumb crumb = breadCrumbs.getCrumb(breadCrumbs.getActiveIndex());
            crumb.setScrollPosition(((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CRUMBS, breadCrumbs.getStateWrapper());
    }

    private void updateAdapter(File directory) {
        ArrayList<File> files = loadFiles(directory);
        if (adapter == null) {
            adapter = new SongFileAdapter(getMainActivity(), files, R.layout.item_list, this, this);
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    checkIsEmpty();
                }
            });
            recyclerView.setAdapter(adapter);
            checkIsEmpty();
        } else {
            adapter.swapDataSet(files);
        }
    }

    @NonNull
    private ArrayList<File> loadFiles(File directory) {
        ArrayList<File> fileList = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            Collections.addAll(fileList, files);
        }
        return fileList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        PreferenceUtil.getInstance(getActivity()).setLastPage(-2);

        getMainActivity().setStatusbarColorAuto();
        getMainActivity().setNavigationbarColorAuto();
        getMainActivity().setTaskDescriptionColorAuto();

        setUpAppbarColor();
        setUpToolbar();
        setUpBreadCrumbs();
        setUpRecyclerView();

        if (savedInstanceState == null) {
            setCrumb(new BreadCrumbLayout.Crumb((File) getArguments().getSerializable(PATH)), true);
        } else {
            breadCrumbs.restoreFromStateWrapper((BreadCrumbLayout.SavedStateWrapper) savedInstanceState.getParcelable(CRUMBS));
            setCrumb(breadCrumbs.getCrumb(breadCrumbs.getActiveIndex()), true);
        }
    }

    private void setUpAppbarColor() {
        int primaryColor = ThemeStore.primaryColor(getActivity());
        appbar.setBackgroundColor(primaryColor);
        toolbar.setBackgroundColor(primaryColor);
        breadCrumbs.setBackgroundColor(primaryColor);
        breadCrumbs.setActivatedContentColor(ToolbarContentTintHelper.toolbarTitleColor(getActivity(), primaryColor));
        breadCrumbs.setDeactivatedContentColor(ToolbarContentTintHelper.toolbarSubtitleColor(getActivity(), primaryColor));
    }

    private void setUpToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        getActivity().setTitle(R.string.app_name);
        getMainActivity().setSupportActionBar(toolbar);
    }

    private void setUpBreadCrumbs() {
        breadCrumbs.setCallback(this);
    }

    private void setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(getActivity(), recyclerView, ThemeStore.accentColor(getActivity()));

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        appbar.addOnOffsetChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveScrollPosition();
    }

    @Override
    public void onDestroyView() {
        appbar.removeOnOffsetChangedListener(this);
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public boolean handleBackPress() {
        if (cab != null && cab.isActive()) {
            cab.finish();
            return true;
        }
        if (breadCrumbs.popHistory()) {
            setCrumb(breadCrumbs.lastHistory(), false);
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    public MaterialCab openCab(int menuRes, MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(getMainActivity(), R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(PhonographColorUtil.shiftBackgroundColorForLightText(ThemeStore.primaryColor(getActivity())))
                .start(callback);
        return cab;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_folders, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_go_to_standard_folder:
                setCrumb(new BreadCrumbLayout.Crumb(getDefaultStartFolder()), true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCrumbSelection(BreadCrumbLayout.Crumb crumb, int index) {
        setCrumb(crumb, true);
    }

    public static File getDefaultStartFolder() {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File musicFolder = new File(externalStorageDir, "Music");
        File startFolder;
        if (musicFolder.exists() && musicFolder.isDirectory()) {
            startFolder = musicFolder;
        } else if (externalStorageDir.exists() && externalStorageDir.isDirectory()) {
            startFolder = externalStorageDir;
        } else {
            startFolder = new File("/"); // root
        }
        return startFolder;
    }

    @Override
    public void onFileSelected(File file) {
        if (file.isDirectory()) {
            setCrumb(new BreadCrumbLayout.Crumb(file), true);
        } else {
            ArrayList<Song> songs = getAllSongs(file.getParentFile(), new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !pathname.isDirectory();
                }
            });

            int startIndex = -1;
            for (int i = 0; i < songs.size(); i++) {
                if (file.getPath().equals(songs.get(i).data)) {
                    startIndex = i;
                    break;
                }
            }
            if (startIndex > -1) {
                MusicPlayerRemote.openQueue(songs, startIndex, true);
            } else {
                Toast.makeText(getActivity(), "Could not play selected file.", Toast.LENGTH_SHORT).show(); // TODO replace with proper text.
            }
        }
    }

    @Nullable
    private static SortedCursor makeSongCursor(@NonNull final Context context, @Nullable final List<File> files) {
        String selection = null;
        String[] values = null;

        if (files != null && files.size() > 0 && files.size() < 999) { // 999 is the max amount Androids SQL implementation can handle.
            selection = AudioColumns.DATA + " IN (" + makePlaceholders(files.size()) + ")";

            values = new String[files.size()];
            for (int i = 0; i < files.size(); i++) {
                values[i] = files.get(i).getPath();
            }
        }

        Cursor songCursor = SongLoader.makeSongCursor(context, selection, values);

        return songCursor == null ? null : new SortedCursor(songCursor, values, AudioColumns.DATA);
    }

    private static String makePlaceholders(int len) {
        StringBuilder sb = new StringBuilder(len * 2 - 1);
        sb.append("?");
        for (int i = 1; i < len; i++) {
            sb.append(",?");
        }
        return sb.toString();
    }

    @Override
    public void onFileMenuClicked(File file) {

    }

    @Override
    public void onAddToPlaylist(ArrayList<File> files) {
        AddToPlaylistDialog.create(getAllSongs(files, null)).show(getFragmentManager(), "ADD_PLAYLIST");
    }

    @Override
    public void onAddToCurrentPlaying(ArrayList<File> files) {
        MusicPlayerRemote.enqueue(getAllSongs(files, null));
    }

    @Override
    public void onDeleteFromDevice(ArrayList<File> files) {
        DeleteSongsDialog.create(getAllSongs(files, null)).show(getFragmentManager(), "DELETE_SONGS");
    }

    private ArrayList<Song> getAllSongs(File dir, @Nullable FileFilter fileFilter) {
        ArrayList<Song> songs = new ArrayList<>();

        ArrayList<File> files = new ArrayList<>();
        File[] fileArray = dir.listFiles(fileFilter);
        if (fileArray != null) Collections.addAll(files, fileArray);
        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            if (file.isDirectory()) {
                songs.addAll(getAllSongs(file, fileFilter));
                iterator.remove();
            }
        }

        SortedCursor cursor = makeSongCursor(getActivity(), files);
        songs.addAll(SongLoader.getSongs(cursor));

        return songs;
    }

    @DebugLog
    private ArrayList<Song> getAllSongs(List<File> files, @Nullable FileFilter fileFilter) {
        ArrayList<Song> songs = new ArrayList<>();

        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            if (fileFilter != null && !fileFilter.accept(file)) {
                iterator.remove();
            } else if (file.isDirectory()) {
                iterator.remove();
                songs.addAll(getAllSongs(file, fileFilter));
            }
        }

        SortedCursor cursor = makeSongCursor(getActivity(), files);
        songs.addAll(SongLoader.getSongs(cursor));

        return songs;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        container.setPadding(container.getPaddingLeft(), container.getPaddingTop(), container.getPaddingRight(), appbar.getTotalScrollRange() + verticalOffset);
    }

    private void checkIsEmpty() {
        if (empty != null) {
            empty.setVisibility(adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }
}