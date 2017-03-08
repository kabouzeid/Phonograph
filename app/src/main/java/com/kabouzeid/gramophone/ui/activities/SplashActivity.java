package com.kabouzeid.gramophone.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Clone intent to pass on filter information
        // such as intent action (for e.g. launcher shortcuts)
        Intent intent = getIntent().cloneFilter();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
