package com.kabouzeid.gramophone.ui.activities.tageditor;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
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
import com.kabouzeid.gramophone.misc.SimpleObservableScrollViewCallbacks;
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

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsTagEditorActivity extends AbsBaseActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_PALETTE = "extra_palette";
    private static final String TAG = AbsTagEditorActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_IMAGE = 1337;
    @Bind(R.id.play_pause_fab)
    FloatingActionButton fab;
    @Bind(R.id.observableScrollView)
    ObservableScrollView observableScrollView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.image)
    ImageView image;
    @Bind(R.id.header)
    LinearLayout header;
    private int id;
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
    private MaterialDialog progressDialog;

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
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(AbsTagEditorActivity.this)
                        .title(R.string.update_image)
                        .items(items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
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
                            }
                        }).show();
            }
        });
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        TintHelper.setTintAuto(fab, ThemeStore.accentColor(this), true);
    }

    protected abstract void save();

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            id = intentExtras.getInt(EXTRA_ID);
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

    protected void writeValuesToFiles(@NonNull final Map<FieldKey, String> fieldKeyValueMap) {
        writeValuesToFiles(fieldKeyValueMap, null, false);
    }

    protected void writeValuesToFiles(@NonNull final Map<FieldKey, String> fieldKeyValueMap, @Nullable final Artwork artwork, final boolean deleteArtwork) {
        Util.hideSoftKeyboard(this);
        final String writingFileStr = getResources().getString(R.string.writing_file_number);
        progressDialog = new MaterialDialog.Builder(AbsTagEditorActivity.this)
                .title(R.string.saving_changes)
                .cancelable(false)
                .progress(true, 0)
                .build();
        if (Build.VERSION.SDK_INT >= 18)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < songPaths.size(); i++) {
                    String songPath = songPaths.get(i);
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setContent(writingFileStr + " " + ((finalI + 1) + "/" + songPaths.size()));
                        }
                    });
                    try {
                        AudioFile audioFile = AudioFileIO.read(new File(songPath));
                        Tag tag = audioFile.getTagOrCreateAndSetDefault();
                        for (Map.Entry<FieldKey, String> entry : fieldKeyValueMap.entrySet()) {
                            try {
                                tag.setField(entry.getKey(), entry.getValue());
                            } catch (NumberFormatException e) {
                                tag.deleteField(entry.getKey());
                            }
                        }
                        if (deleteArtwork) {
                            tag.deleteArtworkField();
                        } else if (artwork != null) {
                            tag.deleteArtworkField();
                            tag.setField(artwork);
                        }
                        audioFile.commit();
                    } catch (@NonNull CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                        Log.e(TAG, "Error while reading audio file.", e);
                    } catch (CannotWriteException e) {
                        Log.e(TAG, "Error while writing audio file.", e);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setContent(getString(R.string.rescanning_media));
                    }
                });
                if (deleteArtwork) {
                    MusicUtil.deleteAlbumArt(AbsTagEditorActivity.this, getId());
                } else if (artwork != null) {
                    // AlbumTagEditorActivity already inserts the album cover for us
                }
                rescanMediaAndQuitOnFinish();
            }
        }).start();
    }

    private static class FinishOnCompletedMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

        private WeakReference<AbsTagEditorActivity> activityWeakReference;
        private MediaScannerConnection connection;
        private String[] toBeScanned;
        private int position;


        private FinishOnCompletedMediaScanner(AbsTagEditorActivity activity, String[] toBeScanned) {
            activityWeakReference = new WeakReference<>(activity);
            this.toBeScanned = toBeScanned;
            connection = new MediaScannerConnection(activity.getApplicationContext(), this);
        }

        public void scan() {
            connection.connect();
        }

        @Override
        public void onMediaScannerConnected() {
            scanNextPath();
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            scanNextPath();
        }

        private void scanNextPath() {
            if (position < toBeScanned.length) {
                connection.scanFile(toBeScanned[position], null);
            }
            checkIsDone();
            position++;
        }

        private void checkIsDone() {
            if (position >= toBeScanned.length - 1) {
                connection.disconnect();
                AbsTagEditorActivity activity = activityWeakReference.get();
                if (activity != null) {
                    activity.dismissDialogAndFinish();
                }
            }
        }
    }

    public void dismissDialogAndFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                finish();
            }
        });
    }

    private void rescanMediaAndQuitOnFinish() {
        String[] toBeScanned = new String[songPaths.size()];
        toBeScanned = songPaths.toArray(toBeScanned);
        new FinishOnCompletedMediaScanner(this, toBeScanned).scan();
    }

    protected int getId() {
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
