package com.kabouzeid.gramophone.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.ColorUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class DonationDialog extends LeakDetectDialogFragment implements BillingProcessor.IBillingHandler {
    public static final String TAG = DonationDialog.class.getSimpleName();

    private static final int DONATION_PRODUCT_IDS = R.array.donation_ids;

    private BillingProcessor billingProcessor;

    public static DonationDialog create() {
        return new DonationDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        billingProcessor = new BillingProcessor(getContext(), App.GOOGLE_PLAY_LICENSE_KEY, this);

        @SuppressLint("InflateParams")
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_donation, null);
        ProgressBar progressBar = ButterKnife.findById(customView, R.id.progress);
        MDTintHelper.setTint(progressBar, ThemeSingleton.get().positiveColor.getDefaultColor());

        return new MaterialDialog.Builder(getContext())
                .title(R.string.support_development)
                .customView(customView, false)
                .build();
    }

    void donate(int i) {
        final String[] ids = getResources().getStringArray(DONATION_PRODUCT_IDS);
        billingProcessor.purchase(getActivity(), ids[i]);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Toast.makeText(getContext(), R.string.thank_you, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Toast.makeText(getContext(), R.string.restored_previous_purchases, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.e(TAG, "Billing error: code = " + errorCode, error);
    }

    @Override
    public void onBillingInitialized() {
        new Thread(new LoadAndDisplayPurchasesRunnable(this)).start();
    }

    @Override
    public void onDestroy() {
        if (billingProcessor != null) {
            billingProcessor.release();
        }
        super.onDestroy();
    }

    static class LoadAndDisplayPurchasesRunnable implements Runnable {
        WeakReference<DonationDialog> dialogWeakReference;

        public LoadAndDisplayPurchasesRunnable(DonationDialog donationDialog) {
            dialogWeakReference = new WeakReference<>(donationDialog);
        }

        @Override
        public void run() {
            try {
                final DonationDialog donationDialog = dialogWeakReference.get();
                final String[] ids = donationDialog.getResources().getStringArray(DONATION_PRODUCT_IDS);
                final List<SkuDetails> skuDetailsList = donationDialog.billingProcessor.getPurchaseListingDetails(new ArrayList<>(Arrays.asList(ids)));

                donationDialog.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (skuDetailsList == null || skuDetailsList.isEmpty()) {
                            donationDialog.dismiss();
                            return;
                        }
                        View customView = ((MaterialDialog) donationDialog.getDialog()).getCustomView();
                        if (customView != null) {
                            customView.findViewById(R.id.progress_container).setVisibility(View.GONE);
                            ListView listView = ButterKnife.findById(customView, R.id.list);
                            listView.setAdapter(new SkuDetailsAdapter(donationDialog, skuDetailsList));
                            listView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class SkuDetailsAdapter extends ArrayAdapter<SkuDetails> {
        @LayoutRes
        private static int LAYOUT_RES_ID = R.layout.item_donation_option;

        DonationDialog donationDialog;

        public SkuDetailsAdapter(@NonNull DonationDialog donationDialog, @NonNull List<SkuDetails> objects) {
            super(donationDialog.getContext(), LAYOUT_RES_ID, objects);
            this.donationDialog = donationDialog;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(LAYOUT_RES_ID, parent, false);
            }

            SkuDetails skuDetails = getItem(position);
            ViewHolder viewHolder = new ViewHolder(convertView);

            viewHolder.title.setText(skuDetails.title.replace("(Phonograph Music Player)", "").trim());
            viewHolder.text.setText(skuDetails.description);
            viewHolder.price.setText(skuDetails.priceText);

            final boolean purchased = donationDialog.billingProcessor.isPurchased(skuDetails.productId);

            int titleTextColor = purchased ? ColorUtil.resolveColor(getContext(), android.R.attr.textColorHint) : ColorUtil.resolveColor(getContext(), android.R.attr.textColorPrimary);
            int contentTextColor = purchased ? titleTextColor : ColorUtil.resolveColor(getContext(), android.R.attr.textColorSecondary);

            viewHolder.title.setTextColor(titleTextColor);
            viewHolder.text.setTextColor(contentTextColor);
            viewHolder.price.setTextColor(titleTextColor);

            strikeThrough(viewHolder.title, purchased);
            strikeThrough(viewHolder.text, purchased);
            strikeThrough(viewHolder.price, purchased);

            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return purchased;
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    donationDialog.donate(position);
                }
            });

            return convertView;
        }

        private static void strikeThrough(TextView textView, boolean strikeThrough) {
            textView.setPaintFlags(strikeThrough ? textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG : textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        static class ViewHolder {
            @Bind(R.id.title)
            TextView title;
            @Bind(R.id.text)
            TextView text;
            @Bind(R.id.price)
            TextView price;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
