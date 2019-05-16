package com.kabouzeid.gramophone.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.misc.UpdateToastMediaScannerCompletionListener;
import com.kabouzeid.gramophone.ui.fragments.mainactivity.folders.FoldersFragment;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad), modified by Karim Abou Zeid
 */
public class ScanMediaFolderChooserDialog extends DialogFragment implements MaterialDialog.ListCallback {

    String initialPath = PreferenceUtil.getInstance(getContext()).getStartDirectory().getAbsolutePath();
    private File parentFolder;
    private File[] parentContents;
    private boolean canGoUp = false;

    public static ScanMediaFolderChooserDialog create() {
        return new ScanMediaFolderChooserDialog();
    }

    private static void scanPaths(@NonNull WeakReference<Activity> activityWeakReference, @NonNull Context applicationContext, @Nullable String[] toBeScanned) {
        Activity activity = activityWeakReference.get();
        if (toBeScanned == null || toBeScanned.length < 1) {
            Toast.makeText(applicationContext, R.string.nothing_to_scan, Toast.LENGTH_SHORT).show();
        } else {
            MediaScannerConnection.scanFile(applicationContext, toBeScanned, null, activity != null ? new UpdateToastMediaScannerCompletionListener(activity, toBeScanned) : null);
        }
    }

    private String[] getContentsArray() {
        if (parentContents == null) {
            if (canGoUp) {
                return new String[]{".."};
            }
            return new String[]{};
        }
        String[] results = new String[parentContents.length + (canGoUp ? 1 : 0)];
        if (canGoUp) {
            results[0] = "..";
        }
        for (int i = 0; i < parentContents.length; i++) {
            results[canGoUp ? i + 1 : i] = parentContents[i].getName();
        }
        return results;
    }

    private File[] listFiles() {
        File[] contents = parentFolder.listFiles();
        List<File> results = new ArrayList<>();
        if (contents != null) {
            for (File fi : contents) {
                if (fi.isDirectory()) {
                    results.add(fi);
                }
            }
            Collections.sort(results, new FolderSorter());
            return results.toArray(new File[results.size()]);
        }
        return null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return new MaterialDialog.Builder(getActivity())
                    .title(R.string.md_error_label)
                    .content(R.string.md_storage_perm_error)
                    .positiveText(android.R.string.ok)
                    .build();
        }
        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }
        if (!savedInstanceState.containsKey("current_path")) {
            savedInstanceState.putString("current_path", initialPath);
        }
        parentFolder = new File(savedInstanceState.getString("current_path", File.pathSeparator));
        checkIfCanGoUp();
        parentContents = listFiles();
        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(getActivity())
                        .title(parentFolder.getAbsolutePath())
                        .items((CharSequence[]) getContentsArray())
                        .itemsCallback(this)
                        .autoDismiss(false)
                        .onPositive((dialog, which) -> {
                            final Context applicationContext = getActivity().getApplicationContext();
                            final WeakReference<Activity> activityWeakReference = new WeakReference<>(getActivity());
                            dismiss();
                            new FoldersFragment.ArrayListPathsAsyncTask(getActivity(), paths -> scanPaths(activityWeakReference, applicationContext, paths)).execute(new FoldersFragment.ArrayListPathsAsyncTask.LoadingInfo(parentFolder, FoldersFragment.AUDIO_FILE_FILTER));
                        })
                        .onNegative((materialDialog, dialogAction) -> dismiss())
                        .positiveText(R.string.action_scan_directory)
                        .negativeText(android.R.string.cancel);
        return builder.build();
    }

    @Override
    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
        if (canGoUp && i == 0) {
            parentFolder = parentFolder.getParentFile();
            if (parentFolder.getAbsolutePath().equals("/storage/emulated")) {
                parentFolder = parentFolder.getParentFile();
            }
            checkIfCanGoUp();
        } else {
            parentFolder = parentContents[canGoUp ? i - 1 : i];
            canGoUp = true;
            if (parentFolder.getAbsolutePath().equals("/storage/emulated")) {
                parentFolder = Environment.getExternalStorageDirectory();
            }
        }
        reload();
    }

    private void checkIfCanGoUp() {
        canGoUp = parentFolder.getParent() != null;
    }

    private void reload() {
        parentContents = listFiles();
        MaterialDialog dialog = (MaterialDialog) getDialog();
        dialog.setTitle(parentFolder.getAbsolutePath());
        dialog.setItems((CharSequence[]) getContentsArray());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("current_path", parentFolder.getAbsolutePath());
    }

    private static class FolderSorter implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}