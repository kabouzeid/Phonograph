package com.kabouzeid.gramophone.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad), modified by Karim Abou Zeid
 */
public class BlacklistFolderChooserDialog extends DialogFragment implements MaterialDialog.ListCallback {

    private File parentFolder;
    private File[] parentContents;
    private boolean canGoUp = false;

    private FolderCallback callback;

    String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();

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

    public static BlacklistFolderChooserDialog create() {
        return new BlacklistFolderChooserDialog();
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
                            dismiss();
                            callback.onFolderSelection(BlacklistFolderChooserDialog.this, parentFolder);
                        })
                        .onNegative((materialDialog, dialogAction) -> dismiss())
                        .positiveText(R.string.add_action)
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

    public void setCallback(FolderCallback callback) {
        this.callback = callback;
    }

    public interface FolderCallback {
        void onFolderSelection(@NonNull BlacklistFolderChooserDialog dialog, @NonNull File folder);
    }

    private static class FolderSorter implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}