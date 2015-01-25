package com.mobeta.android.demodslv;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarpDSLV extends ListActivity {

    private ArrayAdapter<String> mAdapter;

    private final DragSortListView.DropListener mDropListener =
        new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                String item = mAdapter.getItem(from);

                mAdapter.notifyDataSetChanged();
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

    private final DragSortListView.DragScrollProfile mDragScrollProfile =
        new DragSortListView.DragScrollProfile() {
            @Override
            public float getSpeed(float w, long t) {
                if (w > 0.8f) {
                    // Traverse all views in a millisecond
                    return ((float) mAdapter.getCount()) / 0.001f;
                } else {
                    return 10.0f * w;
                }
            }
        };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.warp_main);

        DragSortListView lv = (DragSortListView) getListView(); 

        lv.setDropListener(mDropListener);
        lv.setRemoveListener(mRemoveListener);
        lv.setDragScrollProfile(mDragScrollProfile);

        String[] array = getResources().getStringArray(R.array.countries);
        List<String> list = new ArrayList<String>(Arrays.asList(array));

        mAdapter = new ArrayAdapter<String>(this, R.layout.list_item_handle_right, R.id.text, list);
        setListAdapter(mAdapter);
    }

}
