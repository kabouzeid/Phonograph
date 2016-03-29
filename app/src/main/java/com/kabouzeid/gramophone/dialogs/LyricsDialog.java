package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import hugo.weaving.DebugLog;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LyricsDialog extends DialogFragment {

    public static LyricsDialog create(@NonNull LyricInfo lyricInfo) {
        LyricsDialog dialog = new LyricsDialog();
        Bundle args = new Bundle();
        args.putParcelable("LyricInfo", lyricInfo);
        dialog.setArguments(args);
        return dialog;
    }

    @DebugLog
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LyricInfo lyricInfo = getArguments().getParcelable("LyricInfo");
        //noinspection ConstantConditions
        return new MaterialDialog.Builder(getActivity())
                .title(lyricInfo.title)
                .content(lyricInfo.lyrics)
                .build();
    }

    public static class LyricInfo implements Parcelable {
        public final String title;
        public final String lyrics;

        public LyricInfo(String title, String lyrics) {
            this.title = title;
            this.lyrics = lyrics;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.title);
            dest.writeString(this.lyrics);
        }

        protected LyricInfo(Parcel in) {
            this.title = in.readString();
            this.lyrics = in.readString();
        }

        public static final Parcelable.Creator<LyricInfo> CREATOR = new Parcelable.Creator<LyricInfo>() {
            @Override
            public LyricInfo createFromParcel(Parcel source) {
                return new LyricInfo(source);
            }

            @Override
            public LyricInfo[] newArray(int size) {
                return new LyricInfo[size];
            }
        };
    }
}
