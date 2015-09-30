package com.kabouzeid.gramophone.ui.activities.tageditor;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;
import com.kabouzeid.gramophone.lastfm.rest.model.LastFmAlbum;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.LastFMUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class AlbumTagEditorActivity extends AbsTagEditorActivity implements TextWatcher {

    public static final String TAG = AlbumTagEditorActivity.class.getSimpleName();

    @Bind(R.id.title)
    EditText albumTitle;
    @Bind(R.id.album_artist)
    EditText albumArtist;
    @Bind(R.id.genre)
    EditText genre;
    @Bind(R.id.year)
    EditText year;

    private Bitmap albumArtBitmap;
    private boolean deleteAlbumArt;
    private LastFMRestClient lastFMRestClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        lastFMRestClient = new LastFMRestClient(this);

        setUpViews();
    }

    private void setUpViews() {
        fillViewsWithFileTags();
        albumTitle.addTextChangedListener(this);
        albumArtist.addTextChangedListener(this);
        genre.addTextChangedListener(this);
        year.addTextChangedListener(this);
    }


    private void fillViewsWithFileTags() {
        albumTitle.setText(getAlbumTitle());
        albumArtist.setText(getAlbumArtistName());
        genre.setText(getGenreName());
        year.setText(getSongYear());
    }

    @Override
    protected void loadCurrentImage() {
        setImageBitmap(getAlbumArt());
        deleteAlbumArt = false;
    }

    @Override
    protected void getImageFromLastFM() {
        String albumTitleStr = albumTitle.getText().toString();
        String albumArtistNameStr = albumArtist.getText().toString();
        if (albumArtistNameStr.trim().equals("") || albumTitleStr.trim().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.album_or_artist_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        lastFMRestClient.getApiService().getAlbumInfo(albumTitleStr, albumArtistNameStr).enqueue(new Callback<LastFmAlbum>() {
            @Override
            public void onResponse(final Response<LastFmAlbum> response, Retrofit retrofit) {
                LastFmAlbum lastFmAlbum = response.body();
                if (lastFmAlbum.getAlbum() != null) {
                    ImageLoader.getInstance().loadImage(LastFMUtil.getLargestAlbumImageUrl(lastFmAlbum.getAlbum().getImage()),
                            new DisplayImageOptions.Builder()
                                    .preProcessor(albumCoverProcessor)
                                    .build(),
                            new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    if (loadedImage == null) {
                                        onLoadingFailed(imageUri, view, null);
                                        return;
                                    }
                                    albumArtBitmap = loadedImage;
                                    setImageBitmap(albumArtBitmap);
                                    deleteAlbumArt = false;
                                    dataChanged();
                                    setResult(RESULT_OK);
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    handleFailReason(failReason);
                                }
                            });
                } else {
                    toastLoadingFailed();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                toastLoadingFailed();
            }

            private void toastLoadingFailed() {
                Toast.makeText(AlbumTagEditorActivity.this,
                        R.string.could_not_download_album_cover, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void searchImageOnWeb() {
        searchWebFor(albumTitle.getText().toString(), albumArtist.getText().toString());
    }

    @Override
    protected void deleteImage() {
        setImageRes(R.drawable.default_album_art);
        deleteAlbumArt = true;
        dataChanged();
    }

    @Override
    protected void save() {
        Artwork artwork = null;
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        //android seems not to recognize album_artist field so we additionally write the normal artist field
        fieldKeyValueMap.put(FieldKey.ARTIST, albumArtist.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM_ARTIST, albumArtist.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genre.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());

        File albumArtFile = MusicUtil.createAlbumArtFile(String.valueOf(getId()));

        if (albumArtBitmap != null) {
            try {
                albumArtBitmap.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(albumArtFile));
                artwork = ArtworkFactory.createArtworkFromFile(albumArtFile);
                MusicUtil.insertAlbumArt(this, getId(), albumArtFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "error while trying to create the artwork from file", e);
            }
        }
        writeValuesToFiles(fieldKeyValueMap, artwork, deleteAlbumArt);
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_album_tag_editor;
    }

    @NonNull
    @Override
    protected List<String> getSongPaths() {
        ArrayList<Song> songs = AlbumSongLoader.getAlbumSongList(this, getId());
        ArrayList<String> paths = new ArrayList<>(songs.size());
        for (Song song : songs) {
            paths.add(song.data);
        }
        return paths;
    }

    @Override
    protected void loadImageFromFile(@NonNull final Uri selectedFileUri) {
        ImageLoader.getInstance().loadImage(selectedFileUri.toString(),
                new DisplayImageOptions.Builder()
                        .preProcessor(albumCoverProcessor)
                        .build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        albumArtBitmap = loadedImage;
                        setImageBitmap(albumArtBitmap);
                        deleteAlbumArt = false;
                        dataChanged();
                        setResult(RESULT_OK);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        handleFailReason(failReason);
                    }
                });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        dataChanged();
    }

    private BitmapProcessor albumCoverProcessor = new BitmapProcessor() {
        @Override
        public Bitmap process(Bitmap bitmap) {
            return getResizedAlbumCover(bitmap, Util.getSmallerScreenSize(AlbumTagEditorActivity.this));
        }
    };

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleFailReason(FailReason failReason) {
        Throwable cause = failReason.getCause();
        if (cause != null) {
            cause.printStackTrace();
            Toast.makeText(AlbumTagEditorActivity.this, cause.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private static Bitmap getResizedAlbumCover(@NonNull Bitmap src, int maxForSmallerSize) {
        int width = src.getWidth();
        int height = src.getHeight();

        final int dstWidth;
        final int dstHeight;

        if (width < height) {
            if (maxForSmallerSize >= width) {
                return src;
            }
            float ratio = (float) height / width;
            dstWidth = maxForSmallerSize;
            dstHeight = Math.round(maxForSmallerSize * ratio);
        } else {
            if (maxForSmallerSize >= height) {
                return src;
            }
            float ratio = (float) width / height;
            dstWidth = Math.round(maxForSmallerSize * ratio);
            dstHeight = maxForSmallerSize;
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (scaledBitmap != src) {
            src.recycle();
        }
        return scaledBitmap;
    }
}
