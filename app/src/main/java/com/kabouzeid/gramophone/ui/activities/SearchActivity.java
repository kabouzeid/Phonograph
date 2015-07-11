package com.kabouzeid.gramophone.ui.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.SearchAdapter;
import com.kabouzeid.gramophone.ui.activities.base.AbsBaseActivity;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SearchActivity extends AbsBaseActivity {

    public static final String TAG = SearchActivity.class.getSimpleName();
    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @SuppressWarnings("ButterKnifeNoViewWithId")
    @InjectView(android.R.id.empty)
    TextView empty;


    private SearchView searchView;
    private SearchAdapter searchAdapter;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle(null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.inject(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(this);
        recyclerView.setAdapter(searchAdapter);

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Util.hideSoftKeyboard(SearchActivity.this);
                if (searchView != null) {
                    searchView.clearFocus();
                }
                return false;
            }
        });

        toolbar.setBackgroundColor(PreferenceUtil.getInstance(this).getThemeColorPrimary());
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setNavigationBarThemeColor();
        setStatusBarThemeColor();
    }

    @NonNull
    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void enableViews() {
        super.enableViews();
        recyclerView.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        recyclerView.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem search = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_hint));

        View searchViewPlate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.abc_textfield_search_activated_mtrl_alpha);
        drawable.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
        searchViewPlate.setBackground(drawable);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(@NonNull String query) {
                onQueryTextChange(query);
                Util.hideSoftKeyboard(SearchActivity.this);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(@NonNull String newText) {
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

    private void search(@NonNull String query) {
        if (searchAdapter != null) {
            searchAdapter.search(query);
            empty.setVisibility(searchAdapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
        }
    }
}
