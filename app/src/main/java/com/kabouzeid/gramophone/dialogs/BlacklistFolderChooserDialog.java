package com.kabouzeid.gramophone.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
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

public class BlacklistFolderChooserDialog extends ChooserDialog implements MaterialDialog.ListCallback {

    private FolderCallback callback;


    public static BlacklistFolderChooserDialog create() {
        return new BlacklistFolderChooserDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (isSDKAboveAndroidMarshmallow
                && isNotGrantedPermissionToReadExternalStorage) {
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

        setParentFolder(new File(savedInstanceState.getString("current_path", File.pathSeparator)));

        checkIfCanGoUp();
        setParentContents(listFiles());
        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(getActivity())
                        .title(getParentFolder().getAbsolutePath())
                        .items((CharSequence[]) getContentsArray())
                        .itemsCallback(this)
                        .autoDismiss(false)
                        .onPositive((dialog, which) -> {
                            dismiss();
                            callback.onFolderSelection(BlacklistFolderChooserDialog.this, getParentFolder());
                        })
                        .onNegative((materialDialog, dialogAction) -> dismiss())
                        .positiveText(R.string.add_action)
                        .negativeText(android.R.string.cancel);
        return builder.build();
    }

    @Override
    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
        if (canGoUp && i == 0) {
            setParentFolder(getParentFolder().getParentFile());
            if (getParentFolder().getAbsolutePath().equals("/storage/emulated")) {
                setParentFolder(getParentFolder().getParentFile());
            }
            checkIfCanGoUp();
        } else {
            setParentFolder(getParentContents()[canGoUp ? i - 1 : i]);
            canGoUp = true;
            if (getParentFolder().getAbsolutePath().equals("/storage/emulated")) {
                setParentFolder(Environment.getExternalStorageDirectory());
            }
        }
        reload();
    }


    public void setCallback(FolderCallback callback) {
        this.callback = callback;
    }

    public interface FolderCallback {
        void onFolderSelection(@NonNull BlacklistFolderChooserDialog dialog, @NonNull File folder);
    }


}