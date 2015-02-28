package com.kabouzeid.materialmusic.ui.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.adapter.SearchAdapter;
import com.kabouzeid.materialmusic.loader.AlbumLoader;
import com.kabouzeid.materialmusic.loader.ArtistLoader;
import com.kabouzeid.materialmusic.loader.SongLoader;
import com.kabouzeid.materialmusic.model.Album;
import com.kabouzeid.materialmusic.model.Artist;
import com.kabouzeid.materialmusic.model.SearchEntry;
import com.kabouzeid.materialmusic.model.Song;
import com.kabouzeid.materialmusic.ui.activities.base.AbsBaseActivity;
import com.kabouzeid.materialmusic.util.Util;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AbsBaseActivity {
    public static final String TAG = SearchActivity.class.getSimpleName();

    private ListView listView;
    private SearchView searchView;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpTranslucence(false, false);
        setTitle(null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (Util.hasLollipopSDK()) {
            getWindow().setStatusBarColor(Util.resolveColor(this, R.attr.colorPrimaryDark));
            getWindow().setNavigationBarColor(Util.resolveColor(this, R.attr.colorPrimaryDark));
        }

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item instanceof SearchEntry) {
                    if (item instanceof Song) {
                        List<Song> playList = new ArrayList<>();
                        playList.add((Song) item);
                        getApp().getMusicPlayerRemote().openQueue(playList, 0, true);
                    } else if (item instanceof Album) {
                        goToAlbum(((Album) item).id, new Pair[]{Pair.create(view.findViewById(R.id.image), getResources().getString(R.string.transition_album_cover))});
                    } else if (item instanceof Artist) {
                        goToArtist(((Artist) item).id, new Pair[]{Pair.create(view.findViewById(R.id.image), getResources().getString(R.string.transition_artist_image))});
                    }
                }
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Util.hideSoftKeyboard(SearchActivity.this);
                if(searchView != null){
                    searchView.clearFocus();
                }
                return false;
            }
        });

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void enableViews() {
        super.enableViews();
        listView.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        listView.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem search = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);
                Util.hideSoftKeyboard(SearchActivity.this);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        MenuItemCompat.expandActionView(search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(String query) {
        List<SearchEntry> results = new ArrayList<>();
        if (!query.trim().equals("")) {
            LabelEntry songLabel = new LabelEntry(getResources().getString(R.string.songs).toUpperCase());
            results.add(songLabel);
            List<Song> songs = SongLoader.getSongs(this, query);
            results.addAll(songs);
            songLabel.setNumber(songs.size());

            LabelEntry artistLabel = new LabelEntry(getResources().getString(R.string.artists).toUpperCase());
            results.add(artistLabel);
            List<Artist> artists = ArtistLoader.getArtists(this, query);
            results.addAll(artists);
            artistLabel.setNumber(artists.size());

            LabelEntry albumLabel = new LabelEntry(getResources().getString(R.string.albums).toUpperCase());
            results.add(albumLabel);
            List<Album> albums = AlbumLoader.getAlbums(this, query);
            results.addAll(albums);
            albumLabel.setNumber(albums.size());
        }
        if(results.size() <= 3){
            results.clear();
            results.add(new LabelEntry(getResources().getString(R.string.no_results).toUpperCase()));
        }
        ArrayAdapter adapter = new SearchAdapter(this, results);
        listView.setAdapter(adapter);
    }



    public static class LabelEntry implements SearchEntry {
        String title;
        String label;

        public LabelEntry(String label) {
            this.label = label;
            this.title = label;
        }

        public void setNumber(int number){
            if(number != -1) {
                label = title + " (" + number + ")";
            } else {
                label = title;
            }
        }

        @Override
        public String getTitle() {
            return label;
        }

        @Override
        public String getSubTitle() {
            return "";
        }

        @Override
        public void loadImage(Context context, ImageView imageView) {

        }
    }
}
