package com.kabouzeid.gramophone.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.ChangelogDialog;
import com.kabouzeid.gramophone.dialogs.DonationDialog;
import com.kabouzeid.gramophone.ui.activities.base.AbsBaseActivity;
import com.kabouzeid.gramophone.util.ColorUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
@SuppressWarnings("FieldCanBeLocal")
public class AboutActivity extends AbsBaseActivity implements View.OnClickListener {

    private static String GOOGLE_PLUS = "https://google.com/+KarimAbouZeid23697";
    private static String TWITTER = "https://twitter.com/karim23697";
    private static String GITHUB = "https://github.com/kabouzeid";
    private static String WEBSITE = "http://kabouzeid.de/";

    private static String REPORT_BUGS = "https://github.com/kabouzeid/phonograph-issue-tracker";
    private static String GOOGLE_PLUS_COMMUNITY = "https://plus.google.com/u/0/communities/106227738496107108513";
    private static String TRANSLATE = "https://phonograph.oneskyapp.com/collaboration/project?id=26521";
    private static String RATE_ON_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.kabouzeid.gramophone";

    private static String AIDAN_FOLLESTAD_GOOGLE_PLUS = "https://google.com/+AidanFollestad";
    private static String AIDAN_FOLLESTAD_GITHUB = "https://github.com/afollestad";

    private static String MICHAEL_COOK_GOOGLE_PLUS = "https://google.com/+michaelcook";
    private static String MICHAEL_COOK_WEBSITE = "http://cookicons.co/";

    private static String MAARTEN_CORPEL_GOOGLE_PLUS = "https://google.com/+MaartenCorpel";

    private static String ALEKSANDAR_TESIC_GOOGLE_PLUS = "https://google.com/+aleksandartešić";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.app_version)
    TextView appVersion;
    @Bind(R.id.changelog)
    LinearLayout changelog;
    @Bind(R.id.add_to_google_plus_circles)
    LinearLayout addToGooglePlusCircles;
    @Bind(R.id.follow_on_twitter)
    LinearLayout followOnTwitter;
    @Bind(R.id.fork_on_git_hub)
    LinearLayout forkOnGitHub;
    @Bind(R.id.visit_website)
    LinearLayout visitWebsite;
    @Bind(R.id.report_bugs)
    LinearLayout reportBugs;
    @Bind(R.id.join_google_plus_community)
    LinearLayout joinGooglePlusCommunity;
    @Bind(R.id.translate)
    LinearLayout translate;
    @Bind(R.id.donate)
    LinearLayout donate;
    @Bind(R.id.rate_on_google_play)
    LinearLayout rateOnGooglePlay;
    @Bind(R.id.aidan_follestad_google_plus)
    AppCompatButton aidanFollestadGooglePlus;
    @Bind(R.id.aidan_follestad_git_hub)
    AppCompatButton aidanFollestadGitHub;
    @Bind(R.id.michael_cook_google_plus)
    AppCompatButton michaelCookGooglePlus;
    @Bind(R.id.michael_cook_website)
    AppCompatButton michaelCookWebsite;
    @Bind(R.id.maarten_corpel_google_plus)
    AppCompatButton maartenCorpelGooglePlus;
    @Bind(R.id.aleksandar_tesic_google_plus)
    AppCompatButton aleksandarTesicGooglePlus;

    @Bind(R.id.icon_info)
    ImageView iconInfo;
    @Bind(R.id.icon_changelog)
    ImageView iconChangelog;
    @Bind(R.id.icon_bug_report)
    ImageView iconBugReport;
    @Bind(R.id.icon_google_plus_community)
    ImageView iconGooglePlusCommunity;
    @Bind(R.id.icon_flag)
    ImageView iconFlag;
    @Bind(R.id.icon_rate)
    ImageView iconRate;
    @Bind(R.id.icon_donate)
    ImageView iconDonate;
    @Bind(R.id.icon_author)
    ImageView iconAuthor;
    @Bind(R.id.icon_google_plus)
    ImageView iconGooglePlus;
    @Bind(R.id.icon_twitter)
    ImageView iconTwitter;
    @Bind(R.id.icon_github)
    ImageView iconGithub;
    @Bind(R.id.icon_website)
    ImageView iconWebsite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setStatusBarTransparent();
        ButterKnife.bind(this);

        setUpViews();

        if (shouldColorNavigationBar())
            setNavigationBarThemeColor();
        setStatusBarThemeColor();
    }

    private void setUpViews() {
        setUpToolbar();
        setUpAppVersion();
        setUpIconTint();
        setUpOnClickListeners();
    }

    private void setUpToolbar() {
        toolbar.setBackgroundColor(getThemeColorPrimary());
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpAppVersion() {
        appVersion.setText(getCurrentVersionName(this));
    }

    private void setUpOnClickListeners() {
        changelog.setOnClickListener(this);
        addToGooglePlusCircles.setOnClickListener(this);
        followOnTwitter.setOnClickListener(this);
        forkOnGitHub.setOnClickListener(this);
        visitWebsite.setOnClickListener(this);
        reportBugs.setOnClickListener(this);
        joinGooglePlusCommunity.setOnClickListener(this);
        translate.setOnClickListener(this);
        rateOnGooglePlay.setOnClickListener(this);
        donate.setOnClickListener(this);
        aidanFollestadGooglePlus.setOnClickListener(this);
        aidanFollestadGitHub.setOnClickListener(this);
        michaelCookGooglePlus.setOnClickListener(this);
        michaelCookWebsite.setOnClickListener(this);
        maartenCorpelGooglePlus.setOnClickListener(this);
        aleksandarTesicGooglePlus.setOnClickListener(this);
    }

    private void setUpIconTint() {
        iconInfo.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconChangelog.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconBugReport.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconGooglePlusCommunity.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconFlag.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconRate.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconDonate.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconAuthor.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconGooglePlus.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconTwitter.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconGithub.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
        iconWebsite.setColorFilter(ColorUtil.resolveColor(this, android.R.attr.textColorSecondary));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static String getCurrentVersionName(@NonNull final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0.0.0";
    }

    @Override
    public void onClick(View v) {
        if (v == changelog) {
            ChangelogDialog.create().show(getSupportFragmentManager(), "CHANGELOG_DIALOG");
        } else if (v == addToGooglePlusCircles) {
            openUrl(GOOGLE_PLUS);
        } else if (v == followOnTwitter) {
            openUrl(TWITTER);
        } else if (v == forkOnGitHub) {
            openUrl(GITHUB);
        } else if (v == visitWebsite) {
            openUrl(WEBSITE);
        } else if (v == reportBugs) {
            openUrl(REPORT_BUGS);
        } else if (v == joinGooglePlusCommunity) {
            openUrl(GOOGLE_PLUS_COMMUNITY);
        } else if (v == translate) {
            openUrl(TRANSLATE);
        } else if (v == rateOnGooglePlay) {
            openUrl(RATE_ON_GOOGLE_PLAY);
        } else if (v == donate) {
            DonationDialog.create().show(getSupportFragmentManager(), "DONATION_DIALOG");
        } else if (v == aidanFollestadGooglePlus) {
            openUrl(AIDAN_FOLLESTAD_GOOGLE_PLUS);
        } else if (v == aidanFollestadGitHub) {
            openUrl(AIDAN_FOLLESTAD_GITHUB);
        } else if (v == michaelCookGooglePlus) {
            openUrl(MICHAEL_COOK_GOOGLE_PLUS);
        } else if (v == michaelCookWebsite) {
            openUrl(MICHAEL_COOK_WEBSITE);
        } else if (v == maartenCorpelGooglePlus) {
            openUrl(MAARTEN_CORPEL_GOOGLE_PLUS);
        } else if (v == aleksandarTesicGooglePlus) {
            openUrl(ALEKSANDAR_TESIC_GOOGLE_PLUS);
        }
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}
