package com.kabouzeid.gramophone.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class SongDetailDialog extends DialogFragment {

    public static final String TAG = SongDetailDialog.class.getSimpleName();

    @NonNull
    public static SongDetailDialog create(Song song) {
        SongDetailDialog dialog = new SongDetailDialog();
        Bundle args = new Bundle();
        args.putParcelable("song", song);
        dialog.setArguments(args);
        return dialog;
    }

    private static Spanned makeTextWithTitle(@NonNull Context context, int titleResId, String text) {
        return Html.fromHtml("<b>" + context.getResources().getString(titleResId) + ": " + "</b>" + text);
    }

    private static String getFileSizeString(long sizeInBytes) {
        long fileSizeInKB = sizeInBytes / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;
        return fileSizeInMB + " MB";
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity context = getActivity();
        final Song song = getArguments().getParcelable("song");

        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_file_details, true)
                .title(context.getResources().getString(R.string.label_details))
                .positiveText(android.R.string.ok)
                .build();

        View dialogView = dialog.getCustomView();
        final TextView fileName = dialogView.findViewById(R.id.file_name);
        final TextView filePath = dialogView.findViewById(R.id.file_path);
        final TextView fileSize = dialogView.findViewById(R.id.file_size);
        final TextView fileFormat = dialogView.findViewById(R.id.file_format);
        final TextView trackLength = dialogView.findViewById(R.id.track_length);
        final TextView bitRate = dialogView.findViewById(R.id.bitrate);
        final TextView samplingRate = dialogView.findViewById(R.id.sampling_rate);

        fileName.setText(makeTextWithTitle(context, R.string.label_file_name, "-"));
        filePath.setText(makeTextWithTitle(context, R.string.label_file_path, "-"));
        fileSize.setText(makeTextWithTitle(context, R.string.label_file_size, "-"));
        fileFormat.setText(makeTextWithTitle(context, R.string.label_file_format, "-"));
        trackLength.setText(makeTextWithTitle(context, R.string.label_track_length, "-"));
        bitRate.setText(makeTextWithTitle(context, R.string.label_bit_rate, "-"));
        samplingRate.setText(makeTextWithTitle(context, R.string.label_sampling_rate, "-"));

        if (song != null) {
            final File songFile = new File(song.data);
            if (songFile.exists()) {
                fileName.setText(makeTextWithTitle(context, R.string.label_file_name, songFile.getName()));
                filePath.setText(makeTextWithTitle(context, R.string.label_file_path, songFile.getAbsolutePath()));
                fileSize.setText(makeTextWithTitle(context, R.string.label_file_size, getFileSizeString(songFile.length())));
                try {
                    AudioFile audioFile = AudioFileIO.read(songFile);
                    AudioHeader audioHeader = audioFile.getAudioHeader();

                    fileFormat.setText(makeTextWithTitle(context, R.string.label_file_format, audioHeader.getFormat()));
                    trackLength.setText(makeTextWithTitle(context, R.string.label_track_length, MusicUtil.getReadableDurationString(audioHeader.getTrackLength() * 1000)));
                    bitRate.setText(makeTextWithTitle(context, R.string.label_bit_rate, audioHeader.getBitRate() + " kb/s"));
                    samplingRate.setText(makeTextWithTitle(context, R.string.label_sampling_rate, audioHeader.getSampleRate() + " Hz"));
                } catch (@NonNull CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                    Log.e(TAG, "error while reading the song file", e);
                    // fallback
                    trackLength.setText(makeTextWithTitle(context, R.string.label_track_length, MusicUtil.getReadableDurationString(song.duration)));
                }
            } else {
                // fallback
                fileName.setText(makeTextWithTitle(context, R.string.label_file_name, song.title));
                trackLength.setText(makeTextWithTitle(context, R.string.label_track_length, MusicUtil.getReadableDurationString(song.duration)));
            }
        }

        return dialog;
    }
}
