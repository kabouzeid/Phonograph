package com.mobeta.android.demodslv;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;


public class ArbItemSizeDSLV extends ListActivity {

    private JazzAdapter mAdapter;

    private final DragSortListView.DropListener mDropListener =
            new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            JazzArtist item = mAdapter.getItem(from);

            mAdapter.remove(item);
            mAdapter.insert(item, to);
        }
    };

    private final DragSortListView.RemoveListener mRemoveListener =
            new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which) {
            mAdapter.remove(mAdapter.getItem(which));
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hetero_main);

        DragSortListView lv = (DragSortListView) getListView(); 

        lv.setDropListener(mDropListener);
        lv.setRemoveListener(mRemoveListener);

        String[] artistNames = getResources().getStringArray(R.array.jazz_artist_names);
        String[] artistAlbums = getResources().getStringArray(R.array.jazz_artist_albums);

        int len = artistAlbums.length;
        List<JazzArtist> artists = new ArrayList<JazzArtist>(len);
        JazzArtist ja;
        for (int i = 0; i < len; ++i) {
            ja = new JazzArtist();
            ja.name = artistNames[i];
            if (i < artistAlbums.length) {
                ja.albums = artistAlbums[i];
            } else {
                ja.albums = "No albums listed";
            }
            artists.add(ja);
        }

        mAdapter = new JazzAdapter(artists);
        
        setListAdapter(mAdapter);

    }

    private class JazzArtist {
        public String name;
        public String albums;

        @Override
        public String toString() {
        return name;
        }
    }

    private class ViewHolder {
        public TextView albumsView;
    }

    private class JazzAdapter extends ArrayAdapter<JazzArtist> {
      
        public JazzAdapter(List<JazzArtist> artists) {
            super(ArbItemSizeDSLV.this, R.layout.jazz_artist_list_item,
                    R.id.artist_name_textview, artists);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            if (v != convertView && v != null) {
                ViewHolder holder = new ViewHolder();

                holder.albumsView = (TextView) v.findViewById(R.id.artist_albums_textview);

                v.setTag(holder);
            }

            String albums = getItem(position).albums;

            ViewHolder holder = (ViewHolder) v.getTag();
            holder.albumsView.setText(albums);

            return v;
        }
    }

}
