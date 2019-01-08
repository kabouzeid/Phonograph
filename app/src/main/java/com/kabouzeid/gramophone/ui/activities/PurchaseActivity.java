package com.kabouzeid.gramophone.ui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.kabouzeid.appthemehelper.color.MaterialColor;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.ui.activities.base.AbsBaseActivity;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PurchaseActivity extends AbsBaseActivity implements BillingProcessor.IBillingHandler {

    public static final String TAG = PurchaseActivity.class.getSimpleName();

    private static final int ACTIVITY_COLOR = MaterialColor.Green._500.getAsColor();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.restore_button)
    Button restoreButton;
    @BindView(R.id.purchase_button)
    Button purchaseButton;

    private BillingProcessor billingProcessor;
    private AsyncTask restorePurchaseAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColor(ACTIVITY_COLOR);
        setNavigationbarColor(ACTIVITY_COLOR);
        setTaskDescriptionColor(ACTIVITY_COLOR);

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.buy_pro));

        restoreButton.setEnabled(false);
        purchaseButton.setEnabled(false);

        restoreButton.setOnClickListener(v -> {
            if (restorePurchaseAsyncTask == null || restorePurchaseAsyncTask.getStatus() != AsyncTask.Status.RUNNING) {
                restorePurchase();
            }
        });

        purchaseButton.setOnClickListener(v -> {
            billingProcessor.purchase(PurchaseActivity.this, App.PRO_VERSION_PRODUCT_ID);
        });

        billingProcessor = new BillingProcessor(this, App.GOOGLE_PLAY_LICENSE_KEY, this);
    }

    private void restorePurchase() {
        if (restorePurchaseAsyncTask != null) {
            restorePurchaseAsyncTask.cancel(false);
        }
        restorePurchaseAsyncTask = new RestorePurchaseAsyncTask(this).execute();
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        if (App.isProVersion()) {
            Toast.makeText(this, R.string.restored_previous_purchase_please_restart, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, R.string.no_purchase_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Log.e(TAG, "Billing error: code = " + errorCode, error);
    }

    @Override
    public void onBillingInitialized() {
        restoreButton.setEnabled(true);
        purchaseButton.setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if (billingProcessor != null) {
            billingProcessor.release();
        }
        super.onDestroy();
    }

    private static class RestorePurchaseAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<PurchaseActivity> buyActivityWeakReference;

        public RestorePurchaseAsyncTask(PurchaseActivity purchaseActivity) {
            this.buyActivityWeakReference = new WeakReference<>(purchaseActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PurchaseActivity purchaseActivity = buyActivityWeakReference.get();
            if (purchaseActivity != null) {
                Toast.makeText(purchaseActivity, R.string.restoring_purchase, Toast.LENGTH_SHORT).show();
            } else {
                cancel(false);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            PurchaseActivity purchaseActivity = buyActivityWeakReference.get();
            if (purchaseActivity != null) {
                return purchaseActivity.billingProcessor.loadOwnedPurchasesFromGoogle();
            }
            cancel(false);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            PurchaseActivity purchaseActivity = buyActivityWeakReference.get();
            if (purchaseActivity == null || b == null) return;

            if (b) {
                purchaseActivity.onPurchaseHistoryRestored();
            } else {
                Toast.makeText(purchaseActivity, R.string.could_not_restore_purchase, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
