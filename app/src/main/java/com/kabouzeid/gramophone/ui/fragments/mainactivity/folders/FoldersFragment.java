package com.kabouzeid.gramophone.ui.fragments.mainactivity.folders;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.SongFileAdapter;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.misc.WrappedAsyncTaskLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.ui.fragments.mainactivity.AbsMainActivityFragment;
import com.kabouzeid.gramophone.util.FileUtil;
import com.kabouzeid.gramophone.util.PhonographColorUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.views.BreadCrumbLayout;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

public class FoldersFragment extends AbsMainActivityFragment implements MainActivity.MainActivityFragmentCallbacks, CabHolder, BreadCrumbLayout.SelectionCallback, SongFileAdapter.Callbacks, AppBarLayout.OnOffsetChangedListener, LoaderManager.LoaderCallbacks<List<File>> {
    public static final String TAG = FoldersFragment.class.getSimpleName();

    private static final int LOADER_ID = 1;

    private static final int ADD_TO_PLAYLIST = 0;
    private static final int ADD_TO_CURRENT_PLAYING = 1;
    private static final int DELETE = 2;
    private static final int PLAY = 3;

    protected static final String PATH = "path";
    protected static final String CRUMBS = "crumbs";

    @Bind(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
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

    public FoldersFragment() {
    }

    public static FoldersFragment newInstance(Context context) {
        return newInstance(PreferenceUtil.getInstance(context).getStartDirectory());
    }

    public static FoldersFragment newInstance(File directory) {
        FoldersFragment frag = new FoldersFragment();
        Bundle b = new Bundle();
        b.putSerializable(PATH, directory);
        frag.setArguments(b);
        return frag;
    }

    public void setCrumb(BreadCrumbLayout.Crumb crumb, boolean addToHistory) {
        if (crumb == null) return;
        saveScrollPosition();
        breadCrumbs.setActiveOrAdd(crumb, false);
        if (addToHistory) {
            breadCrumbs.addHistory(crumb);
        }
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void saveScrollPosition() {
        BreadCrumbLayout.Crumb crumb = getActiveCrumb();
        if (crumb != null) {
            crumb.setScrollPosition(((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
        }
    }

    @Nullable
    private BreadCrumbLayout.Crumb getActiveCrumb() {
        return breadCrumbs != null && breadCrumbs.size() > 0 ? breadCrumbs.getCrumb(breadCrumbs.getActiveIndex()) : null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CRUMBS, breadCrumbs.getStateWrapper());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            setCrumb(new BreadCrumbLayout.Crumb(tryGetCanonicalFile((File) getArguments().getSerializable(PATH))), true);
        } else {
            breadCrumbs.restoreFromStateWrapper((BreadCrumbLayout.SavedStateWrapper) savedInstanceState.getParcelable(CRUMBS));
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getMainActivity().setStatusbarColorAuto();
        getMainActivity().setNavigationbarColorAuto();
        getMainActivity().setTaskDescriptionColorAuto();

        setUpAppbarColor();
        setUpToolbar();
        setUpBreadCrumbs();
        setUpRecyclerView();
        setUpAdapter();
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

    private void setUpAdapter() {
        adapter = new SongFileAdapter(getMainActivity(), new LinkedList<File>(), R.layout.item_list, this, this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
        recyclerView.setAdapter(adapter);
        checkIsEmpty();
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
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(getActivity(), toolbar, menu, ATHToolbarActivity.getToolbarBackgroundColor(toolbar));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(getActivity(), toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_go_to_start_directory:
                setCrumb(new BreadCrumbLayout.Crumb(tryGetCanonicalFile(PreferenceUtil.getInstance(getActivity()).getStartDirectory())), true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCrumbSelection(BreadCrumbLayout.Crumb crumb, int index) {
        setCrumb(crumb, true);
    }

    public static File getDefaultStartDirectory() {
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
        file = tryGetCanonicalFile(file); // important as we compare the path value later
        if (file.isDirectory()) {
            setCrumb(new BreadCrumbLayout.Crumb(file), true);
        } else {
            List<File> files = new LinkedList<>();
            files.add(file.getParentFile());

            FileFilter fileFilter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !pathname.isDirectory() && getFileFilter().accept(pathname);
                }
            };

            new ListSongsAsyncTask(this, PLAY, file).execute(new ListSongsAsyncTask.LoadingInfo(files, fileFilter, getFileComparator()));
        }
    }

    @Override
    public void onFileMenuClicked(final File file, View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        if (file.isDirectory()) {
            popupMenu.inflate(R.menu.menu_item_directory);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_set_as_start_directory:
                            PreferenceUtil.getInstance(getActivity()).setStartDirectory(file);
                            return true;
                        case R.id.action_scan:
                            scan(file);
                            return true;
                    }
                    return false;
                }
            });
        } else {
            popupMenu.inflate(R.menu.menu_item_file);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_scan:
                            scan(file);
                            return true;
                    }
                    return false;
                }
            });
        }
        popupMenu.show();
    }

    private void scan(File file) {
        final String[] toBeScanned;

        if (file.isDirectory()) {
            // TODO load async
            List<File> files = FileUtil.listFilesDeep(file, getFileFilter());
            toBeScanned = new String[files.size()];
            for (int i = 0; i < files.size(); i++) {
                File f = files.get(i);
                try {
                    toBeScanned[i] = f.getCanonicalPath(); // canonical path is important here because we want to compare the path with the media store entry later
                } catch (IOException e) {
                    e.printStackTrace();
                    toBeScanned[i] = f.getPath();
                }
            }
        } else {
            toBeScanned = new String[1];
            toBeScanned[0] = file.getPath();
        }

        if (toBeScanned.length < 1) {
            Toast.makeText(getActivity(), R.string.nothing_to_scan, Toast.LENGTH_SHORT).show();
        } else {
            final Toast toast = Toast.makeText(getActivity(), String.format(getString(R.string.scanning), file), Toast.LENGTH_SHORT);
            toast.show();

            MediaScannerConnection.scanFile(getActivity(), toBeScanned, null, new MediaScannerConnection.OnScanCompletedListener() {
                int scanned = 0;
                int failed = 0;

                @Override
                public void onScanCompleted(final String path, final Uri uri) {
                    getActivity().runOnUiThread(new Runnable() {
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void run() {
                            if (uri == null) {
                                failed++;
                            } else {
                                scanned++;
                            }
                            toast.setText(" " + String.format(getString(R.string.scanned_files), scanned, toBeScanned.length) + (failed > 0 ? " " + String.format(getString(R.string.could_not_scan_files), failed) : ""));
                            toast.show();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onAddToPlaylist(ArrayList<File> files) {
        new ListSongsAsyncTask(this, ADD_TO_PLAYLIST, null).execute(new ListSongsAsyncTask.LoadingInfo(files, getFileFilter(), getFileComparator()));
    }

    @Override
    public void onAddToCurrentPlaying(ArrayList<File> files) {
        new ListSongsAsyncTask(this, ADD_TO_CURRENT_PLAYING, null).execute(new ListSongsAsyncTask.LoadingInfo(files, getFileFilter(), getFileComparator()));
    }

    @Override
    public void onDeleteFromDevice(ArrayList<File> files) {
        new ListSongsAsyncTask(this, DELETE, null).execute(new ListSongsAsyncTask.LoadingInfo(files, getFileFilter(), getFileComparator()));
    }

    Comparator<File> fileComparator = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory() && !rhs.isDirectory()) {
                return -1;
            } else if (!lhs.isDirectory() && rhs.isDirectory()) {
                return 1;
            } else {
                return lhs.getName().compareToIgnoreCase
                        (rhs.getName());
            }
        }
    };

    private Comparator<File> getFileComparator() {
        return fileComparator;
    }

    FileFilter audioFileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return !file.isHidden() && (file.isDirectory() || FileUtil.fileIsMimeType(file, "audio/*", MimeTypeMap.getSingleton()));
        }
    };

    private FileFilter getFileFilter() {
        return audioFileFilter;
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

    private static File tryGetCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
            return file;
        }
    }

    private void updateAdapter(@NonNull List<File> files) {
        adapter.swapDataSet(files);
        BreadCrumbLayout.Crumb crumb = getActiveCrumb();
        if (crumb != null && recyclerView != null) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(crumb.getScrollPosition(), 0);
        }
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle args) {
        return new AsyncFileLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
        updateAdapter(data);
    }

    @Override
    public void onLoaderReset(Loader<List<File>> loader) {
        updateAdapter(new LinkedList<File>());
    }

    private static class AsyncFileLoader extends WrappedAsyncTaskLoader<List<File>> {
        private WeakReference<FoldersFragment> fragmentWeakReference;

        public AsyncFileLoader(FoldersFragment foldersFragment) {
            super(foldersFragment.getActivity());
            fragmentWeakReference = new WeakReference<>(foldersFragment);
        }

        @Override
        public List<File> loadInBackground() {
            FoldersFragment foldersFragment = fragmentWeakReference.get();
            File directory = null;
            if (foldersFragment != null) {
                BreadCrumbLayout.Crumb crumb = foldersFragment.getActiveCrumb();
                if (crumb != null) {
                    directory = crumb.getFile();
                }
            }
            if (directory != null) {
                List<File> files = FileUtil.listFiles(directory, foldersFragment.getFileFilter());
                Collections.sort(files, foldersFragment.getFileComparator());
                return files;
            } else {
                return new LinkedList<>();
            }
        }
    }

    private void onSongsListed(int requestCode, ArrayList<Song> songs, Object extra) {
        switch (requestCode) {
            case ADD_TO_PLAYLIST:
                AddToPlaylistDialog.create(songs).show(getFragmentManager(), "ADD_PLAYLIST");
                break;
            case ADD_TO_CURRENT_PLAYING:
                MusicPlayerRemote.enqueue(songs);
                break;
            case DELETE:
                DeleteSongsDialog.create(songs).show(getFragmentManager(), "DELETE_SONGS");
                break;
            case PLAY:
                File file = (File) extra;
                int startIndex = -1;
                for (int i = 0; i < songs.size(); i++) {
                    if (file.getPath().equals(songs.get(i).data)) { // path is already canonical here
                        startIndex = i;
                        break;
                    }
                }
                if (startIndex > -1) {
                    MusicPlayerRemote.openQueue(songs, startIndex, true);
                } else {
                    final File finalFile = file;
                    Snackbar.make(coordinatorLayout, Html.fromHtml(String.format(getString(R.string.not_listed_in_media_store), file.getName())), Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_scan, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    scan(finalFile);
                                }
                            })
                            .setActionTextColor(ThemeStore.accentColor(getActivity()))
                            .show();
                }
                break;
        }
    }

    private static class ListSongsAsyncTask extends DialogAsyncTask<ListSongsAsyncTask.LoadingInfo, Void, ArrayList<Song>> {
        private WeakReference<FoldersFragment> fragmentWeakReference;
        private final int requestCode;
        private final Object extra;

        public ListSongsAsyncTask(FoldersFragment foldersFragment, int requestCode, Object extra) {
            super(foldersFragment.getActivity(), R.string.listing_files);
            this.requestCode = requestCode;
            this.extra = extra;
            fragmentWeakReference = new WeakReference<>(foldersFragment);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            checkFragmentReference();
        }

        @Override
        protected ArrayList<Song> doInBackground(LoadingInfo... params) {
            try {
                LoadingInfo info = params[0];
                List<File> files = FileUtil.listFilesDeep(info.files, info.fileFilter);
                if (isCancelled() || checkFragmentReference()) return null;
                Collections.sort(files, info.fileComparator);
                if (isCancelled() || checkFragmentReference()) return null;
                return FileUtil.matchFilesWithMediaStore(fragmentWeakReference.get().getActivity(), files);
            } catch (Exception e) {
                e.printStackTrace();
                cancel(false);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Song> songs) {
            super.onPostExecute(songs);
            if (!songs.isEmpty() && !checkFragmentReference())
                fragmentWeakReference.get().onSongsListed(requestCode, songs, extra);
        }

        public static class LoadingInfo {
            public final Comparator<File> fileComparator;
            public final FileFilter fileFilter;
            public final List<File> files;

            public LoadingInfo(@NonNull List<File> files, @NonNull FileFilter fileFilter, @NonNull Comparator<File> fileComparator) {
                this.fileComparator = fileComparator;
                this.fileFilter = fileFilter;
                this.files = files;
            }
        }

        /**
         * @return true if the task was canceled
         */
        private boolean checkFragmentReference() {
            if (fragmentWeakReference.get() == null) {
                cancel(false);
                return true;
            }
            return false;
        }
    }

    private static abstract class DialogAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
        private final int title;
        private WeakReference<Context> contextWeakReference;
        private WeakReference<Dialog> dialogWeakReference;

        private boolean supposedToBeDismissed;

        public DialogAsyncTask(Context context, @StringRes int title) {
            contextWeakReference = new WeakReference<>(context);
            dialogWeakReference = new WeakReference<>(null);
            this.title = title;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!supposedToBeDismissed && contextWeakReference.get() != null) {
                        Dialog dialog = new MaterialDialog.Builder(contextWeakReference.get())
                                .title(title)
                                .progress(true, 0)
                                .progressIndeterminateStyle(true)
                                .cancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        cancel(false);
                                    }
                                })
                                .dismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        cancel(false);
                                    }
                                })
                                .negativeText(android.R.string.cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        cancel(false);
                                    }
                                })
                                .show();
                        dialogWeakReference = new WeakReference<>(dialog);
                    }
                }
            }, 200);
        }

        @DebugLog
        @Override
        protected void onCancelled(Result result) {
            super.onCancelled(result);
            tryToDismiss();
        }

        @DebugLog
        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            tryToDismiss();
        }

        private void tryToDismiss() {
            supposedToBeDismissed = true;
            try {
                if (dialogWeakReference.get() != null)
                    dialogWeakReference.get().dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}