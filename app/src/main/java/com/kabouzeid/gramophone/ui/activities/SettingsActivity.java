package com.kabouzeid.gramophone.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.ColorChooserDialog;
import com.kabouzeid.gramophone.model.UIPreferenceChangedEvent;
import com.kabouzeid.gramophone.prefs.ColorChooserPreference;
import com.kabouzeid.gramophone.ui.activities.base.AbsBaseActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;

import java.util.Set;

public class SettingsActivity extends AbsBaseActivity implements ColorChooserDialog.ColorCallback {
    public static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setStatusBarTranslucent(!Util.hasLollipopSDK());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(PreferenceUtils.getInstance(this).getThemeColorPrimary());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }

    @Override
    protected boolean shouldColorStatusBar() {
        return true;
    }

    @Override
    protected boolean shouldColorNavBar() {
        return PreferenceUtils.getInstance(this).coloredNavigationBarOtherScreensEnabled();
    }

    @Override
    public void onColorSelection(int title, int color) {
        if (title == R.string.primary_color) {
            PreferenceUtils.getInstance(this).setThemeColorPrimary(color);
            App.bus.post(new UIPreferenceChangedEvent(UIPreferenceChangedEvent.THEME_CHANGED, color));
        } else if (title == R.string.accent_color) {
            PreferenceUtils.getInstance(this).setThemeColorAccent(color);
            App.bus.post(new UIPreferenceChangedEvent(UIPreferenceChangedEvent.THEME_CHANGED, color));
        }
        recreate();
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
                    App.bus.post(new UIPreferenceChangedEvent(UIPreferenceChangedEvent.THEME_CHANGED, o));
                    return true;
                }
            });

            ColorChooserPreference primaryColor = (ColorChooserPreference) findPreference("primary_color");
            primaryColor.setColor(PreferenceUtils.getInstance(getActivity()).getThemeColorPrimary(),
                    DialogUtils.resolveColor(getActivity(), android.R.attr.textColorPrimary));
            primaryColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog().show(getActivity(), preference.getTitleRes(),
                            PreferenceUtils.getInstance(getActivity()).getThemeColorPrimary());
                    return true;
                }
            });

            ColorChooserPreference accentColor = (ColorChooserPreference) findPreference("accent_color");
            accentColor.setColor(PreferenceUtils.getInstance(getActivity()).getThemeColorAccent(),
                    DialogUtils.resolveColor(getActivity(), android.R.attr.textColorPrimary));
            accentColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog().show(getActivity(), preference.getTitleRes(),
                            PreferenceUtils.getInstance(getActivity()).getThemeColorAccent());
                    return true;
                }
            });

            findPreference("transparent_toolbar").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    App.bus.post(new UIPreferenceChangedEvent(UIPreferenceChangedEvent.TOOLBAR_TRANSPARENT_CHANGED, o));
                    return true;
                }
            });

            findPreference("colored_album_footers").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    App.bus.post(new UIPreferenceChangedEvent(UIPreferenceChangedEvent.ALBUM_OVERVIEW_PALETTE_CHANGED, o));
                    return true;
                }
            });

            findPreference("playback_controller_card").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    App.bus.post(new UIPreferenceChangedEvent(UIPreferenceChangedEvent.PLAYBACK_CONTROLLER_CARD_CHANGED, o));
                    return true;
                }
            });

            Preference colorNavBar = findPreference("colored_navigation_bar");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                colorNavBar.setEnabled(false);
                colorNavBar.setSummary(R.string.pref_only_lollipop);
            } else {
                colorNavBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        App.bus.post(new UIPreferenceChangedEvent(UIPreferenceChangedEvent.COLORED_NAVIGATION_BAR_CHANGED, o));
                        return true;
                    }
                });
            }

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
    protected void onUIPreferenceChangedEvent(UIPreferenceChangedEvent event) {
        super.onUIPreferenceChangedEvent(event);
        switch (event.getAction()) {
            case UIPreferenceChangedEvent.COLORED_NAVIGATION_BAR_OTHER_SCREENS_CHANGED:
                setShouldColorNavBar((boolean) event.getValue());
                break;
            case UIPreferenceChangedEvent.COLORED_NAVIGATION_BAR_CHANGED:
                try {
                    setShouldColorNavBar(((Set) event.getValue()).contains(PreferenceUtils.COLORED_NAVIGATION_BAR_OTHER_SCREENS));
                } catch (NullPointerException ignored) {
                    setShouldColorNavBar(false);
                }
                break;
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
