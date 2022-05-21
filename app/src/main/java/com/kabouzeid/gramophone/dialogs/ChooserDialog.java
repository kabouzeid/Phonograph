package com.kabouzeid.gramophone.dialogs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChooserDialog extends DialogFragment{
    String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private File parentFolder;
    private File[] parentContents;

    protected File getParentFolder() {
        return parentFolder;
    }

    protected void setParentFolder(File parentFolder) {
        this.parentFolder = parentFolder;
    }

    protected File[] getParentContents() {
        return parentContents;
    }

    protected void setParentContents(File[] parentContents) {
        this.parentContents = parentContents;
    }

    protected boolean isCanGoUp() {
        return canGoUp;
    }

    protected void setCanGoUp(boolean canGoUp) {
        this.canGoUp = canGoUp;
    }

    protected boolean canGoUp = false;

    protected final boolean isSDKAboveAndroidMarshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    protected final boolean isNotGrantedPermissionToReadExternalStorage = ActivityCompat.checkSelfPermission(
            getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED;

    protected String[] getContentsArray() {
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

    protected File[] listFiles() {
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

    protected void checkIfCanGoUp() {
        canGoUp = parentFolder.getParent() != null;
    }

    protected void reload() {
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

    protected static class FolderSorter implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
