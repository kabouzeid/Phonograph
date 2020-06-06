package com.kabouzeid.gramophone.preferences;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Html;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.BlocklistFolderChooserDialog;
import com.kabouzeid.gramophone.provider.BlocklistStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class BlocklistPreferenceDialog extends DialogFragment implements BlocklistFolderChooserDialog.FolderCallback {

    private List<String> paths;

    public static BlocklistPreferenceDialog newInstance() {
        return new BlocklistPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BlocklistFolderChooserDialog blocklistFolderChooserDialog = (BlocklistFolderChooserDialog) getChildFragmentManager().findFragmentByTag("FOLDER_CHOOSER");
        if (blocklistFolderChooserDialog != null) {
            blocklistFolderChooserDialog.setCallback(this);
        }

        refreshBlocklistData();
        return new MaterialDialog.Builder(getContext())
                .title(R.string.blocklist)
                .positiveText(android.R.string.ok)
                .neutralText(R.string.clear_action)
                .negativeText(R.string.add_action)
                .items(paths)
                .autoDismiss(false)
                .itemsCallback((materialDialog, view, i, charSequence) -> new MaterialDialog.Builder(getContext())
                        .title(R.string.remove_from_blocklist)
                        .content(Html.fromHtml(getString(R.string.do_you_want_to_remove_from_the_blocklist, charSequence)))
                        .positiveText(R.string.remove_action)
                        .negativeText(android.R.string.cancel)
                        .onPositive((materialDialog12, dialogAction) -> {
                            BlocklistStore.getInstance(getContext()).removePath(new File(charSequence.toString()));
                            refreshBlocklistData();
                        }).show())
                // clear
                .onNeutral((materialDialog, dialogAction) -> new MaterialDialog.Builder(getContext())
                        .title(R.string.clear_blocklist)
                        .content(R.string.do_you_want_to_clear_the_blocklist)
                        .positiveText(R.string.clear_action)
                        .negativeText(android.R.string.cancel)
                        .onPositive((materialDialog1, dialogAction1) -> {
                            BlocklistStore.getInstance(getContext()).clear();
                            refreshBlocklistData();
                        }).show())
                // add
                .onNegative((materialDialog, dialogAction) -> {
                    BlocklistFolderChooserDialog dialog = BlocklistFolderChooserDialog.create();
                    dialog.setCallback(BlocklistPreferenceDialog.this);
                    dialog.show(getChildFragmentManager(), "FOLDER_CHOOSER");
                })
                .onPositive((materialDialog, dialogAction) -> dismiss())
                .build();
    }

    private void refreshBlocklistData() {
        paths = BlocklistStore.getInstance(getContext()).getPaths();

        MaterialDialog dialog = (MaterialDialog) getDialog();
        if (dialog != null) {
            String[] pathArray = new String[paths.size()];
            pathArray = paths.toArray(pathArray);
            dialog.setItems((CharSequence[]) pathArray);
        }
    }

    @Override
    public void onFolderSelection(@NonNull BlocklistFolderChooserDialog folderChooserDialog, @NonNull File file) {
        BlocklistStore.getInstance(getContext()).addPath(file);
        refreshBlocklistData();
    }
}
