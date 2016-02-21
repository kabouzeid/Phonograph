package com.kabouzeid.gramophone.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.SearchAdapter;
import com.kabouzeid.gramophone.ui.activities.base.AbsMusicServiceActivity;
import com.kabouzeid.gramophone.util.Util;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchActivity extends AbsMusicServiceActivity {

    public static final String TAG = SearchActivity.class.getSimpleName();
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.search_src_text)
    EditText searchSrcText;
    @Bind(R.id.search_close_btn)
    ImageView searchCloseBtn;
    @SuppressWarnings("ButterKnifeNoViewWithId")
    @Bind(android.R.id.empty)
    TextView empty;

    private SearchAdapter searchAdapter;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setDrawUnderStatusbar(true);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(this);
        recyclerView.setAdapter(searchAdapter);

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });

        setUpToolBar();
        setUpSearchBar();
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSearchBar() {
        searchCloseBtn.setColorFilter(ToolbarContentTintHelper.toolbarContentColor(this, ThemeStore.primaryColor(this)), PorterDuff.Mode.SRC_IN);
        searchCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSrcText.setText("");
            }
        });

        searchSrcText.setTextColor(ToolbarContentTintHelper.toolbarTitleColor(this, ThemeStore.primaryColor(this)));
        searchSrcText.setHintTextColor(ToolbarContentTintHelper.toolbarSubtitleColor(this, ThemeStore.primaryColor(this)));
        searchSrcText.setHint(R.string.search_hint);

        searchSrcText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(searchSrcText.getText().toString());
            }
        });

        searchSrcText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(searchSrcText.getText().toString());
                    hideSoftKeyboard();
                    return true;
                }
                return false;
            }
        });
    }

    private void hideSoftKeyboard() {
        Util.hideSoftKeyboard(SearchActivity.this);
        searchSrcText.clearFocus();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(@NonNull String query) {
        if (searchAdapter != null) {
            searchAdapter.search(query);
            empty.setVisibility(searchAdapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        search(searchSrcText.getText().toString());
    }
}
