package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.BuildConfig;
import com.kabouzeid.gramophone.R;

import java.lang.ref.WeakReference;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class BuyDialog extends DialogFragment implements BillingProcessor.IBillingHandler {
    public static final String TAG = BuyDialog.class.getSimpleName();

    private BillingProcessor billingProcessor;

    private AsyncTask skuDetailsLoadAsyncTask;

    public static BuyDialog create() {
        return new BuyDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        billingProcessor = new BillingProcessor(getContext(), App.GOOGLE_PLAY_LICENSE_KEY, this);
        return new MaterialDialog.Builder(getContext())
                .title(R.string.buy_pro)
                .content("Unlock all features, such as:\n• Folder view\n• All theme colors\n• Black theme\n• Sleep timer")
                .positiveText(R.string.buy)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        billingProcessor.purchase(getActivity(), App.PRO_VERSION_PRODUCT_ID);
                    }
                })
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        loadSkuDetails();
        Toast.makeText(getContext(), R.string.thank_you, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        loadSkuDetails();
        Toast.makeText(getContext(), R.string.restored_previous_purchases_please_restart, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.e(TAG, "Billing error: code = " + errorCode, error);
    }

    @Override
    public void onBillingInitialized() {
        loadSkuDetails();
    }

    @Override
    public void onDestroy() {
        if (billingProcessor != null) {
            billingProcessor.release();
        }
        if (skuDetailsLoadAsyncTask != null) {
            skuDetailsLoadAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    private void loadSkuDetails() {
        if (skuDetailsLoadAsyncTask != null) {
            skuDetailsLoadAsyncTask.cancel(false);
        }
        skuDetailsLoadAsyncTask = new SkuDetailsLoadAsyncTask(this).execute();
    }

    private static class SkuDetailsLoadAsyncTask extends AsyncTask<Void, Void, SkuDetails> {
        private final WeakReference<BuyDialog> donationDialogWeakReference;

        public SkuDetailsLoadAsyncTask(BuyDialog donationsDialog) {
            this.donationDialogWeakReference = new WeakReference<>(donationsDialog);
        }

        @Override
        protected SkuDetails doInBackground(Void... params) {
            BuyDialog dialog = donationDialogWeakReference.get();
            if (dialog != null) {
                return dialog.billingProcessor.getPurchaseListingDetails(App.PRO_VERSION_PRODUCT_ID);
            }
            cancel(false);
            return null;
        }

        @Override
        protected void onPostExecute(SkuDetails skuDetails) {
            super.onPostExecute(skuDetails);
            BuyDialog dialog = donationDialogWeakReference.get();
            if (dialog == null) return;

            if (skuDetails == null) {
                if (!BuildConfig.DEBUG) dialog.dismiss();
                return;
            }

            MDButton positiveButton = ((MaterialDialog) dialog.getDialog()).getActionButton(DialogAction.POSITIVE);
            positiveButton.setText(String.format("%s %s", dialog.getString(R.string.buy), skuDetails.priceText));
        }
    }
}
