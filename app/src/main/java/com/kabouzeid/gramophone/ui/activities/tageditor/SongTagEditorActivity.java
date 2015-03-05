package com.kabouzeid.gramophone.ui.activities.tageditor;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.SongFilePathLoader;

import org.jaudiotagger.tag.FieldKey;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SongTagEditorActivity extends AbsTagEditorActivity implements TextWatcher {
    public static final String TAG = SongTagEditorActivity.class.getSimpleName();

    private EditText songTitle;
    private EditText albumTitle;
    private EditText artistName;
    private EditText genreName;
    private EditText year;
    private EditText trackNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setNoImageMode();
        initViews();
        setUpViews();
    }

    private void initViews() {
        songTitle = (EditText) findViewById(R.id.title1);
        albumTitle = (EditText) findViewById(R.id.title2);
        artistName = (EditText) findViewById(R.id.artist);
        genreName = (EditText) findViewById(R.id.genre);
        year = (EditText) findViewById(R.id.year);
        trackNumber = (EditText) findViewById(R.id.track_number);
    }

    private void setUpViews() {
        fillViewsWithFileTags();
        songTitle.addTextChangedListener(this);
        albumTitle.addTextChangedListener(this);
        artistName.addTextChangedListener(this);
        genreName.addTextChangedListener(this);
        year.addTextChangedListener(this);
        trackNumber.addTextChangedListener(this);
    }


    private void fillViewsWithFileTags() {
        songTitle.setText(getSongTitle());
        albumTitle.setText(getAlbumTitle());
        artistName.setText(getArtistName());
        genreName.setText(getGenreName());
        year.setText(getSongYear());
        trackNumber.setText(getTrackNumber());
    }

    @Override
    protected void loadCurrentImage() {

    }

    @Override
    protected void getImageFromLastFM() {

    }

    @Override
    protected void searchImageOnWeb() {

    }

    @Override
    protected void deleteImage() {

    }

    @Override
    protected void save() {
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        fieldKeyValueMap.put(FieldKey.TITLE, songTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ARTIST, artistName.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genreName.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());
        fieldKeyValueMap.put(FieldKey.TRACK, trackNumber.getText().toString());
        writeValuesToFiles(fieldKeyValueMap);
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.activity_song_tag_editor;
    }

    @Override
    protected List<String> getSongPaths() {
        return SongFilePathLoader.getSongFilePaths(this, new int[]{getId()});
    }

    @Override
    protected void loadImageFromFile(Uri imageFilePath) {

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
