package com.kabouzeid.gramophone.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.UiPreferenceChangedEvent;
import com.kabouzeid.gramophone.ui.activities.base.AbsBaseActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;

public class SettingsActivity extends AbsBaseActivity {
    public static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        private Preference equalizer;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            addPreferencesFromResource(R.xml.pref_ui);
            addPreferencesFromResource(R.xml.pref_audio);

            final Preference defaultStartPage = findPreference("default_start_page");
            setSummary(defaultStartPage);
            defaultStartPage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    setSummary(defaultStartPage, o);
                    return true;
                }
            });

            final Preference generalTheme = findPreference("general_theme");
            setSummary(generalTheme);
            generalTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    setSummary(generalTheme, o);
                    App.bus.post(new UiPreferenceChangedEvent(UiPreferenceChangedEvent.THEME_CHANGED, o));
                    return true;
                }
            });

            findPreference("transparent_toolbar").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    App.bus.post(new UiPreferenceChangedEvent(UiPreferenceChangedEvent.TOOLBAR_TRANSPARENT_CHANGED, o));
                    return true;
                }
            });

            findPreference("colored_album_footers").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    App.bus.post(new UiPreferenceChangedEvent(UiPreferenceChangedEvent.ALBUM_OVERVIEW_PALETTE_CHANGED, o));
                    return true;
                }
            });

            findPreference("colored_navigation_bar_artist").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    App.bus.post(new UiPreferenceChangedEvent(UiPreferenceChangedEvent.COLORED_NAVIGATION_BAR_ARTIST_CHANGED, o));
                    return true;
                }
            });

            findPreference("colored_navigation_bar_album").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    App.bus.post(new UiPreferenceChangedEvent(UiPreferenceChangedEvent.COLORED_NAVIGATION_BAR_ALBUM_CHANGED, o));
                    return true;
                }
            });

            findPreference("playback_controller_card").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    App.bus.post(new UiPreferenceChangedEvent(UiPreferenceChangedEvent.PLAYBACK_CONTROLLER_CARD_CHANGED, o));
                    return true;
                }
            });

            equalizer = findPreference("equalizer");
            resolveEqualizer();
            equalizer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    NavigationUtil.openEqualizer(getActivity());
                    return true;
                }
            });
        }

        private static void setSummary(Preference preference) {
            setSummary(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }

        private static void setSummary(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        private void resolveEqualizer() {
            final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            PackageManager pm = getActivity().getPackageManager();
            ResolveInfo ri = pm.resolveActivity(effects, 0);
            if (ri == null) {
                equalizer.setEnabled(false);
                equalizer.setSummary(getResources().getString(R.string.no_equalizer));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
