package com.mobeta.android.demodslv;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

import java.util.ArrayList;
import java.util.Arrays;


public class MultipleChoiceListView extends ListActivity {

    private ArrayAdapter<String> adapter;

    private final DragSortListView.DropListener mDropListener =
        new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    DragSortListView list = getListView();
                    String item = adapter.getItem(from);
                    adapter.remove(item);
                    adapter.insert(item, to);
                    list.moveCheckState(from, to);
                }
            }
        };

    private final RemoveListener mRemoveListener =
        new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                DragSortListView list = getListView();
                String item = adapter.getItem(which);
                adapter.remove(item);
                list.removeCheckState(which);
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkable_main);
        
        String[] array = getResources().getStringArray(R.array.jazz_artist_names);
        ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(array));

        adapter = new ArrayAdapter<String>(this, R.layout.list_item_checkable, R.id.text, arrayList);
        
        setListAdapter(adapter);
        
        DragSortListView list = getListView();
        list.setDropListener(mDropListener);
        list.setRemoveListener(mRemoveListener);
   }

    @Override
    public DragSortListView getListView() {
        return (DragSortListView) super.getListView();
    }
    
}
