package com.kabouzeid.gramophone.helper;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.Util;

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
 * Created by karim on 19.01.15.
 */
public class SongDetailDialogHelper {
    public static final String TAG = SongDetailDialogHelper.class.getSimpleName();

    public static MaterialDialog getDialog(final Context context, final File songFile) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_file_details, true)
                .title(context.getResources().getString(R.string.label_details))
                .positiveText(context.getResources().getString(R.string.ok))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .build();

        View dialogView = dialog.getCustomView();
        final TextView fileName = (TextView) dialogView.findViewById(R.id.file_name);
        final TextView filePath = (TextView) dialogView.findViewById(R.id.file_path);
        final TextView fileSize = (TextView) dialogView.findViewById(R.id.file_size);
        final TextView fileFormat = (TextView) dialogView.findViewById(R.id.file_format);
        final TextView trackLength = (TextView) dialogView.findViewById(R.id.track_length);
        final TextView bitRate = (TextView) dialogView.findViewById(R.id.bitrate);
        final TextView samplingRate = (TextView) dialogView.findViewById(R.id.sampling_rate);

        fileName.setText(makeTextWithTitle(context, R.string.label_file_name, "-"));
        filePath.setText(makeTextWithTitle(context, R.string.label_file_path, "-"));
        fileSize.setText(makeTextWithTitle(context, R.string.label_file_size, "-"));
        fileFormat.setText(makeTextWithTitle(context, R.string.label_file_format, "-"));
        trackLength.setText(makeTextWithTitle(context, R.string.label_track_length, "-"));
        bitRate.setText(makeTextWithTitle(context, R.string.label_bit_rate, "-"));
        samplingRate.setText(makeTextWithTitle(context, R.string.label_sampling_rate, "-"));

        try {
            if (songFile != null && songFile.exists()) {
                AudioFile audioFile = AudioFileIO.read(songFile);
                AudioHeader audioHeader = audioFile.getAudioHeader();

                fileName.setText(makeTextWithTitle(context, R.string.label_file_name, songFile.getName()));
                filePath.setText(makeTextWithTitle(context, R.string.label_file_path, songFile.getAbsolutePath()));
                fileSize.setText(makeTextWithTitle(context, R.string.label_file_size, Util.getFileSizeString(songFile.length())));
                fileFormat.setText(makeTextWithTitle(context, R.string.label_file_format, audioHeader.getFormat()));
                trackLength.setText(makeTextWithTitle(context, R.string.label_track_length, MusicUtil.getReadableDurationString(audioHeader.getTrackLength() * 1000)));
                bitRate.setText(makeTextWithTitle(context, R.string.label_bit_rate, audioHeader.getBitRate() + " kb/s"));
                samplingRate.setText(makeTextWithTitle(context, R.string.label_sampling_rate, audioHeader.getSampleRate() + " Hz"));
            }
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            Log.e(TAG, "error while reading the song file", e);
        }
        return dialog;
    }

    private static Spanned makeTextWithTitle(Context context, int titleResId, String text) {
        return Html.fromHtml("<b>" + context.getResources().getString(titleResId) + ": " + "</b>" + text);
    }
}
