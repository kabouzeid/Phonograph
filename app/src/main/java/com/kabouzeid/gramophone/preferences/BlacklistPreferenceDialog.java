package com.kabouzeid.gramophone.preferences;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Html;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.BlacklistFolderChooserDialog;
import com.kabouzeid.gramophone.provider.BlacklistStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class BlacklistPreferenceDialog extends DialogFragment implements BlacklistFolderChooserDialog.FolderCallback {

    private List<String> paths;

    public static BlacklistPreferenceDialog newInstance() {
        return new BlacklistPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BlacklistFolderChooserDialog blacklistFolderChooserDialog = (BlacklistFolderChooserDialog) getChildFragmentManager().findFragmentByTag("FOLDER_CHOOSER");
        if (blacklistFolderChooserDialog != null) {
            blacklistFolderChooserDialog.setCallback(this);
        }

        refreshBlacklistData();
        return new MaterialDialog.Builder(getContext())
                .title(R.string.blacklist)
                .positiveText(android.R.string.ok)
                .neutralText(R.string.clear_action)
                .negativeText(R.string.add_action)
                .items(paths)
                .autoDismiss(false)
                .itemsCallback((materialDialog, view, i, charSequence) -> new MaterialDialog.Builder(getContext())
                        .title(R.string.remove_from_blacklist)
                        .content(Html.fromHtml(getString(R.string.do_you_want_to_remove_from_the_blacklist, charSequence)))
                        .positiveText(R.string.remove_action)
                        .negativeText(android.R.string.cancel)
                        .onPositive((materialDialog12, dialogAction) -> {
                            BlacklistStore.getInstance(getContext()).removePath(new File(charSequence.toString()));
                            refreshBlacklistData();
                        }).show())
                // clear
                .onNeutral((materialDialog, dialogAction) -> new MaterialDialog.Builder(getContext())
                        .title(R.string.clear_blacklist)
                        .content(R.string.do_you_want_to_clear_the_blacklist)
                        .positiveText(R.string.clear_action)
                        .negativeText(android.R.string.cancel)
                        .onPositive((materialDialog1, dialogAction1) -> {
                            BlacklistStore.getInstance(getContext()).clear();
                            refreshBlacklistData();
                        }).show())
                // add
                .onNegative((materialDialog, dialogAction) -> {
                    BlacklistFolderChooserDialog dialog = BlacklistFolderChooserDialog.create();
                    dialog.setCallback(BlacklistPreferenceDialog.this);
                    dialog.show(getChildFragmentManager(), "FOLDER_CHOOSER");
                })
                .onPositive((materialDialog, dialogAction) -> dismiss())
                .build();
    }

    private void refreshBlacklistData() {
        paths = BlacklistStore.getInstance(getContext()).getPaths();

        MaterialDialog dialog = (MaterialDialog) getDialog();
        if (dialog != null) {
            String[] pathArray = new String[paths.size()];
            pathArray = paths.toArray(pathArray);
            dialog.setItems((CharSequence[]) pathArray);
        }
    }

    @Override
    public void onFolderSelection(@NonNull BlacklistFolderChooserDialog folderChooserDialog, @NonNull File file) {
        BlacklistStore.getInstance(getContext()).addPath(file);
        refreshBlacklistData();
    }
}
