package com.kabouzeid.materialmusic.ui.activities.tageditor;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.lastfm.album.LastFMAlbumImageLoader;
import com.kabouzeid.materialmusic.loader.AlbumSongLoader;
import com.kabouzeid.materialmusic.loader.SongFileLoader;
import com.kabouzeid.materialmusic.model.Song;
import com.kabouzeid.materialmusic.util.MusicUtil;
import com.kabouzeid.materialmusic.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
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

public class AlbumTagEditorActivity extends AbsTagEditorActivity implements TextWatcher {
    public static final String TAG = AlbumTagEditorActivity.class.getSimpleName();

    private File albumArtFile;
    private Bitmap albumArtBitmap;
    private boolean deleteAlbumArt;

    private EditText albumTitle;
    private EditText albumArtistName;
    private EditText genreName;
    private EditText year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
        setUpViews();
    }

    private void initViews() {
        albumTitle = (EditText) findViewById(R.id.album_title);
        albumArtistName = (EditText) findViewById(R.id.album_artist);
        genreName = (EditText) findViewById(R.id.genre);
        year = (EditText) findViewById(R.id.year);
    }

    private void setUpViews() {
        fillViewsWithFileTags();
        albumTitle.addTextChangedListener(this);
        albumArtistName.addTextChangedListener(this);
        genreName.addTextChangedListener(this);
        year.addTextChangedListener(this);
    }


    private void fillViewsWithFileTags() {
        albumTitle.setText(getAlbumTitle());
        albumArtistName.setText(getAlbumArtistName());
        genreName.setText(getGenreName());
        year.setText(getSongYear());
    }

    @Override
    protected void save() {
        Artwork artwork = null;
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        //android seems not to recognize album_artist field so we additionally write the normal artist field
        fieldKeyValueMap.put(FieldKey.ARTIST, albumArtistName.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM_ARTIST, albumArtistName.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genreName.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());

        try {
            albumArtFile = MusicUtil.getAlbumArtFile(this, String.valueOf(getId()));
        } catch (IOException e) {
            Log.e(TAG, "error while creating albumArtFile", e);
        }

        if (albumArtBitmap != null && albumArtFile != null) {
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
    protected int getContentViewResId() {
        return R.layout.activity_album_tag_editor;
    }

    @Override
    protected void loadCurrentImage() {
        setImageBitmap(getAlbumArt());
        deleteAlbumArt = false;
    }

    @Override
    protected void getImageFromLastFM() {
        String albumTitleStr = albumTitle.getText().toString();
        String albumArtistNameStr = albumArtistName.getText().toString();
        if (albumArtistNameStr.trim().equals("") || albumTitleStr.trim().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.album_or_artist_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        LastFMAlbumImageLoader.loadAlbumImage(this, albumTitleStr, albumArtistNameStr, new LastFMAlbumImageLoader.AlbumImageLoaderCallback() {
            @Override
            public void onAlbumImageLoaded(Bitmap albumImage, String uri) {
                if (albumImage != null) {
                    setImageBitmap(albumImage);
                    albumArtBitmap = albumImage;
                    deleteAlbumArt = false;
                    dataChanged();
                    Toast.makeText(AlbumTagEditorActivity.this, "Success.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AlbumTagEditorActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void searchImageOnWeb() {
        List<String> query = new ArrayList<>();
        query.add(albumTitle.getText().toString());
        query.add(albumArtistName.getText().toString());
        searchWebFor(query);
    }

    @Override
    protected void loadImageFromFile(final Uri selectedFileUri) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .postProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bmp) {
                        return Util.getAlbumArtScaledBitmap(bmp, true);
                    }
                })
                .build();
        albumArtBitmap = ImageLoader.getInstance().loadImageSync(selectedFileUri.toString(), options);
        if (albumArtBitmap != null) {
            setImageBitmap(albumArtBitmap);
            deleteAlbumArt = false;
            dataChanged();
        }
    }

    @Override
    protected void deleteImage() {
        setImageRes(R.drawable.default_album_art);
        deleteAlbumArt = true;
        dataChanged();
    }

    @Override
    protected List<String> getSongPaths() {
        List<Song> songs = AlbumSongLoader.getAlbumSongList(this, getId());
        List<Integer> songIds = new ArrayList<>();
        for (Song song : songs) {
            songIds.add(song.id);
        }
        return SongFileLoader.getSongFiles(this, songIds);
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
}
