package com.kabouzeid.gramophone;

import android.content.Context;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

public class Billing {

    public static final String GOOGLE_PLAY_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjMeADN5Ffnt/ml5SYxNPCn8kGcOYGpHEfNSCts99vVxqmCn6C01E94c17j7rUK2aeHur5uxphZylzopPlQ8P8l1fqty0GPUNRSo18FCJzfGH8HZAwZYOcnRFPaXdaq3InyFJhBiODh2oeAcVK/idH6QraQ4r9HIlzigAg6lgwzxl2wJKDh7X/GMdDntCyzDh8xDQ0wIawFgvgojHwqh2Ci8Gnq6EYRwPA9yHiIIksT8Q30QyM5ewl5QcnWepsls7enNqeHarhpmSibRUDgCsxHoOpny7SyuvZvUI3wuLckDR0ds9hrt614scHHqDOBp/qWCZiAgOPVAEQcURbV09qQIDAQAB";
    public static final String PRO_VERSION_PRODUCT_ID = "pro_version";

    private BillingProcessor billingProcessor;

    public Billing(final Context context) {
        // automatically restores purchases
        billingProcessor = new BillingProcessor(context, GOOGLE_PLAY_LICENSE_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {
            }

            @Override
            public void onPurchaseHistoryRestored() {
//                Toast.makeText(context, R.string.restored_previous_purchase_please_restart, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {
            }

            @Override
            public void onBillingInitialized() {
            }
        });
    }

    public boolean isPurchased() {
        return billingProcessor.isPurchased(PRO_VERSION_PRODUCT_ID);
    }

    public void release() {
        billingProcessor.release();
    }
}
