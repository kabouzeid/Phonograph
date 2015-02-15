package com.kabouzeid.materialmusic.ui.activities.tageditor;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.misc.SmallObservableScrollViewCallbacks;
import com.kabouzeid.materialmusic.util.MusicUtil;
import com.kabouzeid.materialmusic.util.Util;
import com.kabouzeid.materialmusic.util.ViewUtil;
import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

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
import java.util.List;
import java.util.Map;

/**
 * Created by karim on 18.01.15.
 */
public abstract class AbsTagEditorActivity extends ActionBarActivity {
    public static final String TAG = AbsTagEditorActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_IMAGE = 1337;

    private App app;
    private int id;
    private int headerVariableSpace;
    private int paletteColorPrimary;
    private boolean isInNoImageMode;

    private FloatingActionButton fab;
    private ObservableScrollView scrollView;
    private Toolbar toolBar;
    private ImageView image;
    private View header;
    private SmallObservableScrollViewCallbacks observableScrollViewCallbacks = new SmallObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            float alpha;
            if (!isInNoImageMode) {
                alpha = 1 - (float) Math.max(0, headerVariableSpace - scrollY) / headerVariableSpace;
            } else {
                ViewHelper.setTranslationY(header, scrollY);
                alpha = 1;
            }
            ViewUtil.setBackgroundAlpha(toolBar, alpha, paletteColorPrimary);
            ViewUtil.setBackgroundAlpha(header, alpha, paletteColorPrimary);
            ViewHelper.setTranslationY(image, scrollY / 2);
        }
    };
    private List<String> songPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (App) getApplicationContext();
        setTheme(app.getAppTheme());
        setUpTranslucence();

        super.onCreate(savedInstanceState);
        setContentView(getContentViewResId());

        getIntentExtras();
        headerVariableSpace = getResources().getDimensionPixelSize(R.dimen.tagEditorHeaderVariableSpace);
        songPaths = getSongPaths();

        initViews();
        setUpViews();
        setUpToolBar();
    }

    private void initViews() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        scrollView = (ObservableScrollView) findViewById(R.id.observableScrollView);
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        image = (ImageView) findViewById(R.id.image);
        header = findViewById(R.id.header);
    }

    private void setUpViews() {
        restoreStandardColors();
        setUpScrollView();
        setUpFab();
        setUpImageView();
    }

    private void setUpScrollView() {
        scrollView.setScrollViewCallbacks(observableScrollViewCallbacks);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollVerticallyTo(headerVariableSpace / 2);
            }
        });
    }

    private void setUpImageView() {
        loadCurrentImage();
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(AbsTagEditorActivity.this)
                        .title("Update image")
                        .items(new CharSequence[]{"Download from LastFM", "Pick from internal storage", "Web search", "Delete"})
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
                        })
                        .build()
                        .show();
            }
        });
    }

    private void startImagePicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_SELECT_IMAGE);
    }

    protected abstract void loadCurrentImage();

    protected abstract void getImageFromLastFM();

    protected abstract void searchImageOnWeb();

    protected abstract void deleteImage();

    protected void setUpFab() {
        ViewHelper.setScaleX(fab, 0);
        ViewHelper.setScaleY(fab, 0);
        fab.setEnabled(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    protected abstract void save();

    private void restoreStandardColors() {
        final int vibrantColor = Util.resolveColor(this, R.attr.colorPrimary);
        paletteColorPrimary = vibrantColor;
        observableScrollViewCallbacks.onScrollChanged(scrollView.getCurrentScrollY(), false, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(vibrantColor);
            getWindow().setNavigationBarColor(vibrantColor);
        }
    }

    private void setUpTranslucence() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow(), false);
            Util.setNavBarTranslucent(getWindow(), false);
        }
    }

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            id = intentExtras.getInt(AppKeys.E_ID);
        }
    }

    protected void setUpToolBar() {
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle(getResources().getString(R.string.tag_editor));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected abstract int getContentViewResId();

    protected abstract List<String> getSongPaths();

    protected void searchWebFor(List<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            stringBuilder.append(string);
            stringBuilder.append(" ");
        }
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, stringBuilder.toString());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tag_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setNoImageMode() {
        isInNoImageMode = true;
        image.setVisibility(View.GONE);
        image.setEnabled(false);
        scrollView.setPadding(0, Util.getActionBarSize(this), 0, 0);
        observableScrollViewCallbacks.onScrollChanged(scrollView.getCurrentScrollY(), false, false);
    }

    protected void dataChanged() {
        showFab();
    }

    private void showFab() {
        ViewPropertyAnimator.animate(fab)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .scaleX(1)
                .scaleY(1)
                .start();
        fab.setEnabled(true);
    }

    protected void setImageRes(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        setImageBitmap(bitmap);
    }

    protected void setImageBitmap(final Bitmap bitmap) {
        if (bitmap != null) {
            image.setImageBitmap(bitmap);
            applyPalette(bitmap);
        }
    }

    private void applyPalette(final Bitmap bitmap) {
        if (bitmap != null) {
            Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    final int vibrantColor = palette.getVibrantColor(Util.resolveColor(AbsTagEditorActivity.this, R.attr.colorPrimary));
                    paletteColorPrimary = vibrantColor;
                    observableScrollViewCallbacks.onScrollChanged(scrollView.getCurrentScrollY(), false, false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(vibrantColor);
                        getWindow().setNavigationBarColor(vibrantColor);
                    }
                }
            });
        } else {
            restoreStandardColors();
        }
    }

    protected void writeValuesToFiles(final Map<FieldKey, String> fieldKeyValueMap) {
        writeValuesToFiles(fieldKeyValueMap, null, false);
    }

    protected void writeValuesToFiles(final Map<FieldKey, String> fieldKeyValueMap, final Artwork artwork, final boolean deleteArtwork) {
        Util.hideSoftKeyboard(this);
        final String writingFileStr = getResources().getString(R.string.writing_file_number);
        final String savingStr = getResources().getString(R.string.saving_changes);
        final MaterialDialog progressDialog = new MaterialDialog.Builder(AbsTagEditorActivity.this)
                .title(savingStr)
                .cancelable(false)
                .progress(true, 0)
                .build();
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
                    } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                        Log.e(TAG, "Error while reading audio file.", e);
                    } catch (CannotWriteException e) {
                        Log.e(TAG, "Error while writing audio file.", e);
                    }
                }
                if (deleteArtwork) {
                    String imagePath = MusicUtil.getAlbumArtUri(getId()).toString();
                    ImageLoader.getInstance().getDiskCache().remove(imagePath);
                    MemoryCacheUtils.removeFromCache(imagePath, ImageLoader.getInstance().getMemoryCache());
                    MusicUtil.deleteAlbumArt(AbsTagEditorActivity.this, getId());
                } else if (artwork != null) {
                    String imagePath = MusicUtil.getAlbumArtUri(getId()).toString();
                    MemoryCacheUtils.removeFromCache(imagePath, ImageLoader.getInstance().getMemoryCache());
                    ImageLoader.getInstance().getDiskCache().remove(imagePath);
                }
                progressDialog.dismiss();
                rescanMedia();
                restartApp();
            }
        }).start();
    }

    private void rescanMedia() {
        String[] toBeScanned = new String[songPaths.size()];
        toBeScanned = songPaths.toArray(toBeScanned);
        MediaScannerConnection.scanFile(this, toBeScanned, null, null);
    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    protected int getId() {
        return id;
    }

    protected void writeValuesToFiles(final Map<FieldKey, String> fieldKeyValueMap, final Artwork artwork) {
        if (artwork == null) {
            writeValuesToFiles(fieldKeyValueMap, null, true);
        } else {
            writeValuesToFiles(fieldKeyValueMap, artwork, false);
        }
    }

    protected void writeValuesToFiles(final Map<FieldKey, String> fieldKeyValueMap, boolean deleteArtwork) {
        writeValuesToFiles(fieldKeyValueMap, null, deleteArtwork);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
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

    protected App getApp() {
        return app;
    }

    protected String getSongTitle() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.TITLE);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private AudioFile getAudioFile(String path) {
        try {
            return AudioFileIO.read(new File(path));
        } catch (CannotReadException | ReadOnlyFileException | InvalidAudioFrameException | TagException | IOException e) {
            Log.e(TAG, "Error while trying to create the AudioFile from java.io.File", e);
        }
        return null;
    }

    protected String getAlbumTitle() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.ALBUM);
        } catch (NullPointerException ignored) {}
        return null;
    }

    protected String getArtistName() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.ARTIST);
        } catch (NullPointerException ignored) {}
        return null;
    }

    protected String getAlbumArtistName() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.ALBUM_ARTIST);
        } catch (NullPointerException ignored) {}
        return null;
    }

    protected String getGenreName() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.GENRE);
        } catch (NullPointerException ignored) {}
        return null;
    }

    protected String getSongYear() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.YEAR);
        } catch (NullPointerException ignored) {}
        return null;
    }

    protected String getTrackNumber() {
        try {
            return getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirst(FieldKey.TRACK);
        } catch (NullPointerException ignored) {}
        return null;
    }

    protected Bitmap getAlbumArt() {
        try {
            Artwork artworkTag = getAudioFile(songPaths.get(0)).getTagOrCreateAndSetDefault().getFirstArtwork();
            if (artworkTag != null) {
                byte[] artworkBinaryData = artworkTag.getBinaryData();
                return BitmapFactory.decodeByteArray(artworkBinaryData, 0, artworkBinaryData.length);
            }
        } catch (NullPointerException ignored) {}
        return null;
    }
}
