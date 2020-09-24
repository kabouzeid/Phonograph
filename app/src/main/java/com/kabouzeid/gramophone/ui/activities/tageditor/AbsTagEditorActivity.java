package com.kabouzeid.gramophone.ui.activities.tageditor;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.misc.DialogAsyncTask;
import com.kabouzeid.gramophone.misc.SimpleObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.misc.UpdateToastMediaScannerCompletionListener;
import com.kabouzeid.gramophone.ui.activities.base.AbsBaseActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.Util;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsTagEditorActivity extends AbsBaseActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_PALETTE = "extra_palette";
    private static final String TAG = AbsTagEditorActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_IMAGE = 1000;
    @BindView(R.id.play_pause_fab)
    FloatingActionButton fab;
    @BindView(R.id.observableScrollView)
    ObservableScrollView observableScrollView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.header)
    LinearLayout header;
    private long id;
    private int headerVariableSpace;
    private int paletteColorPrimary;
    private boolean isInNoImageMode;
    private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            float alpha;
            if (!isInNoImageMode) {
                alpha = 1 - (float) Math.max(0, headerVariableSpace - scrollY) / headerVariableSpace;
            } else {
                header.setTranslationY(scrollY);
                alpha = 1;
            }
            toolbar.setBackgroundColor(ColorUtil.withAlpha(paletteColorPrimary, alpha));
            image.setTranslationY(scrollY / 2);
        }
    };
    private List<String> songPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayout());
        ButterKnife.bind(this);

        getIntentExtras();

        songPaths = getSongPaths();
        if (songPaths.isEmpty()) {
            finish();
            return;
        }

        headerVariableSpace = getResources().getDimensionPixelSize(R.dimen.tagEditorHeaderVariableSpace);

        setUpViews();

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpViews() {
        setUpScrollView();
        setUpFab();
        setUpImageView();
    }

    private void setUpScrollView() {
        observableScrollView.setScrollViewCallbacks(observableScrollViewCallbacks);
    }

    private void setUpImageView() {
        loadCurrentImage();
        final CharSequence[] items = new CharSequence[]{
                getString(R.string.download_from_last_fm),
                getString(R.string.pick_from_local_storage),
                getString(R.string.web_search),
                getString(R.string.remove_cover)
        };
        image.setOnClickListener(v -> new MaterialDialog.Builder(AbsTagEditorActivity.this)
                .title(R.string.update_image)
                .items(items)
                .itemsCallback((dialog, view, which, text) -> {
                    switch (which) {
                        case 0:
                            getImageFromLastFM();
                            break;
                        case 1:
                            startImagePicker();
                            break;
                        case 2:
                            searchImageOnWeb();
                            break;
                        case 3:
                            deleteImage();
                            break;
                    }
                }).show());
    }

    private void startImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_from_local_storage)), REQUEST_CODE_SELECT_IMAGE);
    }

    protected abstract void loadCurrentImage();

    protected abstract void getImageFromLastFM();

    protected abstract void searchImageOnWeb();

    protected abstract void deleteImage();

    private void setUpFab() {
        fab.setScaleX(0);
        fab.setScaleY(0);
        fab.setEnabled(false);
        fab.setOnClickListener(v -> save());

        TintHelper.setTintAuto(fab, ThemeStore.accentColor(this), true);
    }

    protected abstract void save();

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            id = intentExtras.getLong(EXTRA_ID);
        }
    }

    protected abstract int getContentViewLayout();

    @NonNull
    protected abstract List<String> getSongPaths();

    protected void searchWebFor(String... keys) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keys) {
            stringBuilder.append(key);
            stringBuilder.append(" ");
        }
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, stringBuilder.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setNoImageMode() {
        isInNoImageMode = true;
        image.setVisibility(View.GONE);
        image.setEnabled(false);
        observableScrollView.setPadding(0, Util.getActionBarSize(this), 0, 0);
        observableScrollViewCallbacks.onScrollChanged(observableScrollView.getCurrentScrollY(), false, false);

        setColors(getIntent().getIntExtra(EXTRA_PALETTE, ThemeStore.primaryColor(this)));
        toolbar.setBackgroundColor(paletteColorPrimary);
    }

    protected void dataChanged() {
        showFab();
    }

    private void showFab() {
        fab.animate()
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .scaleX(1)
                .scaleY(1)
                .start();
        fab.setEnabled(true);
    }

    protected void setImageBitmap(@Nullable final Bitmap bitmap, int bgColor) {
        if (bitmap == null) {
            image.setImageResource(R.drawable.default_album_art);
        } else {
            image.setImageBitmap(bitmap);
        }
        setColors(bgColor);
    }

    protected void setColors(int color) {
        paletteColorPrimary = color;
        observableScrollViewCallbacks.onScrollChanged(observableScrollView.getCurrentScrollY(), false, false);
        header.setBackgroundColor(paletteColorPrimary);
        setStatusbarColor(paletteColorPrimary);
        setNavigationbarColor(paletteColorPrimary);
        setTaskDescriptionColor(paletteColorPrimary);
    }

    protected void writeValuesToFiles(@NonNull final Map<FieldKey, String> fieldKeyValueMap, @Nullable final ArtworkInfo artworkInfo) {
        Util.hideSoftKeyboard(this);

        new WriteTagsAsyncTask(this).execute(new WriteTagsAsyncTask.LoadingInfo(getSongPaths(), fieldKeyValueMap, artworkInfo));
    }

    private static class WriteTagsAsyncTask extends DialogAsyncTask<WriteTagsAsyncTask.LoadingInfo, Integer, String[]> {
        Context applicationContext;

        public WriteTagsAsyncTask(Context context) {
            super(context);
            applicationContext = context;
        }

        @Override
        protected String[] doInBackground(LoadingInfo... params) {
            try {
                LoadingInfo info = params[0];

                Artwork artwork = null;
                File albumArtFile = null;
                if (info.artworkInfo != null && info.artworkInfo.artwork != null) {
                    try {
                        albumArtFile = MusicUtil.createAlbumArtFile().getCanonicalFile();
                        info.artworkInfo.artwork.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(albumArtFile));
                        artwork = ArtworkFactory.createArtworkFromFile(albumArtFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                int counter = 0;
                boolean wroteArtwork = false;
                boolean deletedArtwork = false;
                for (String filePath : info.filePaths) {
                    publishProgress(++counter, info.filePaths.size());
                    try {
                        AudioFile audioFile = AudioFileIO.read(new File(filePath));
                        Tag tag = audioFile.getTagOrCreateAndSetDefault();

                        if (info.fieldKeyValueMap != null) {
                            for (Map.Entry<FieldKey, String> entry : info.fieldKeyValueMap.entrySet()) {
                                try {
                                    tag.setField(entry.getKey(), entry.getValue());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (info.artworkInfo != null) {
                            if (info.artworkInfo.artwork == null) {
                                tag.deleteArtworkField();
                                deletedArtwork = true;
                            } else if (artwork != null) {
                                tag.deleteArtworkField();
                                tag.setField(artwork);
                                wroteArtwork = true;
                            }
                        }

                        audioFile.commit();
                    } catch (@NonNull CannotReadException | IOException | CannotWriteException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                        e.printStackTrace();
                    }
                }

                Context context = getContext();
                if (context != null) {
                    if (wroteArtwork) {
                        MusicUtil.insertAlbumArt(context, info.artworkInfo.albumId, albumArtFile.getPath());
                    } else if (deletedArtwork) {
                        MusicUtil.deleteAlbumArt(context, info.artworkInfo.albumId);
                    }
                }

                return info.filePaths.toArray(new String[info.filePaths.size()]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] toBeScanned) {
            super.onPostExecute(toBeScanned);
            scan(toBeScanned);
        }

        @Override
        protected void onCancelled(String[] toBeScanned) {
            super.onCancelled(toBeScanned);
            scan(toBeScanned);
        }

        private void scan(String[] toBeScanned) {
            Context context = getContext();
            MediaScannerConnection.scanFile(applicationContext, toBeScanned, null, context instanceof Activity ? new UpdateToastMediaScannerCompletionListener((Activity) context, toBeScanned) : null);
        }

        @Override
        protected Dialog createDialog(@NonNull Context context) {
            return new MaterialDialog.Builder(context)
                    .title(R.string.saving_changes)
                    .cancelable(false)
                    .progress(false, 0)
                    .build();
        }

        @Override
        protected void onProgressUpdate(@NonNull Dialog dialog, Integer... values) {
            super.onProgressUpdate(dialog, values);
            ((MaterialDialog) dialog).setMaxProgress(values[1]);
            ((MaterialDialog) dialog).setProgress(values[0]);
        }

        public static class LoadingInfo {
            public final Collection<String> filePaths;
            @Nullable
            public final Map<FieldKey, String> fieldKeyValueMap;
            @Nullable
            private ArtworkInfo artworkInfo;

            private LoadingInfo(Collection<String> filePaths, @Nullable Map<FieldKey, String> fieldKeyValueMap, @Nullable ArtworkInfo artworkInfo) {
                this.filePaths = filePaths;
                this.fieldKeyValueMap = fieldKeyValueMap;
                this.artworkInfo = artworkInfo;
            }
        }
    }

    public static class ArtworkInfo {
        public final long albumId;
        public final Bitmap artwork;

        public ArtworkInfo(long albumId, Bitmap artwork) {
            this.albumId = albumId;
            this.artwork = artwork;
        }
    }

    protected long getId() {
        return id;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    loadImageFromFile(selectedImage);
                }
                break;
        }
    }

    protected abstract void loadImageFromFile(Uri selectedFile);

    @NonNull
    private AudioFile getAudioFile(@NonNull String path) {
        try {
            return AudioFileIO.read(new File(path));
        } catch (Exception e) {
            Log.e(TAG, "Could not read audio file " + path, e);
            return new AudioFile();
        }
    }

    @Nullable
    protected String getSongTitle() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.TITLE);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    protected String getAlbumTitle() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.ALBUM);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    protected String getArtistName() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.ARTIST);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    protected String getAlbumArtistName() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.ALBUM_ARTIST);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    protected String getGenreName() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.GENRE);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    protected String getSongYear() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.YEAR);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    protected String getTrackNumber() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.TRACK);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    protected String getLyrics() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.LYRICS);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    protected Bitmap getAlbumArt() {
        try {
            Artwork artworkTag = getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirstArtwork();
            if (artworkTag != null) {
                byte[] artworkBinaryData = artworkTag.getBinaryData();
                return BitmapFactory.decodeByteArray(artworkBinaryData, 0, artworkBinaryData.length);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
