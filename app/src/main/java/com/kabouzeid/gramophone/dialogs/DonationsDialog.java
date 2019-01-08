package com.kabouzeid.gramophone.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class DonationsDialog extends DialogFragment implements BillingProcessor.IBillingHandler {
    public static final String TAG = DonationsDialog.class.getSimpleName();

    private static final int DONATION_PRODUCT_IDS = R.array.donation_ids;

    private BillingProcessor billingProcessor;

    private AsyncTask skuDetailsLoadAsyncTask;

    public static DonationsDialog create() {
        return new DonationsDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        billingProcessor = new BillingProcessor(getContext(), App.GOOGLE_PLAY_LICENSE_KEY, this);

        @SuppressLint("InflateParams")
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_donation, null);
        ProgressBar progressBar = customView.findViewById(R.id.progress);
        MDTintHelper.setTint(progressBar, ThemeSingleton.get().positiveColor.getDefaultColor());

        return new MaterialDialog.Builder(getContext())
                .title(R.string.support_development)
                .customView(customView, false)
                .build();
    }

    private void donate(int i) {
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
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        loadSkuDetails();
        Toast.makeText(getContext(), R.string.thank_you, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        loadSkuDetails();
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

    private static class SkuDetailsLoadAsyncTask extends AsyncTask<Void, Void, List<SkuDetails>> {
        private final WeakReference<DonationsDialog> donationDialogWeakReference;

        public SkuDetailsLoadAsyncTask(DonationsDialog donationsDialog) {
            this.donationDialogWeakReference = new WeakReference<>(donationsDialog);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DonationsDialog dialog = donationDialogWeakReference.get();
            if (dialog == null) return;

            View customView = ((MaterialDialog) dialog.getDialog()).getCustomView();
            //noinspection ConstantConditions
            customView.findViewById(R.id.progress_container).setVisibility(View.VISIBLE);
            customView.findViewById(R.id.list).setVisibility(View.GONE);
        }

        @Override
        protected List<SkuDetails> doInBackground(Void... params) {
            DonationsDialog dialog = donationDialogWeakReference.get();
            if (dialog != null) {
                final String[] ids = dialog.getResources().getStringArray(DONATION_PRODUCT_IDS);
                return dialog.billingProcessor.getPurchaseListingDetails(new ArrayList<>(Arrays.asList(ids)));
            }
            cancel(false);
            return null;
        }

        @Override
        protected void onPostExecute(List<SkuDetails> skuDetails) {
            super.onPostExecute(skuDetails);
            DonationsDialog dialog = donationDialogWeakReference.get();
            if (dialog == null) return;

            if (skuDetails == null || skuDetails.isEmpty()) {
                dialog.dismiss();
                return;
            }

            View customView = ((MaterialDialog) dialog.getDialog()).getCustomView();
            //noinspection ConstantConditions
            customView.findViewById(R.id.progress_container).setVisibility(View.GONE);
            ListView listView = customView.findViewById(R.id.list);
            listView.setAdapter(new SkuDetailsAdapter(dialog, skuDetails));
            listView.setVisibility(View.VISIBLE);
        }
    }

    static class SkuDetailsAdapter extends ArrayAdapter<SkuDetails> {
        @LayoutRes
        private static int LAYOUT_RES_ID = R.layout.item_donation_option;

        DonationsDialog donationsDialog;

        public SkuDetailsAdapter(@NonNull DonationsDialog donationsDialog, @NonNull List<SkuDetails> objects) {
            super(donationsDialog.getContext(), LAYOUT_RES_ID, objects);
            this.donationsDialog = donationsDialog;
        }

        @Override
        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(LAYOUT_RES_ID, parent, false);
            }

            SkuDetails skuDetails = getItem(position);
            ViewHolder viewHolder = new ViewHolder(convertView);

            viewHolder.title.setText(skuDetails.title.replace("(Phonograph Music Player)", "").trim());
            viewHolder.text.setText(skuDetails.description);
            viewHolder.price.setText(skuDetails.priceText);

            final boolean purchased = donationsDialog.billingProcessor.isPurchased(skuDetails.productId);
            int titleTextColor = purchased ? ATHUtil.resolveColor(getContext(), android.R.attr.textColorHint) : ThemeStore.textColorPrimary(getContext());
            int contentTextColor = purchased ? titleTextColor : ThemeStore.textColorSecondary(getContext());

            //noinspection ResourceAsColor
            viewHolder.title.setTextColor(titleTextColor);
            //noinspection ResourceAsColor
            viewHolder.text.setTextColor(contentTextColor);
            //noinspection ResourceAsColor
            viewHolder.price.setTextColor(titleTextColor);

            strikeThrough(viewHolder.title, purchased);
            strikeThrough(viewHolder.text, purchased);
            strikeThrough(viewHolder.price, purchased);

            convertView.setOnTouchListener((v, event) -> purchased);

            convertView.setOnClickListener(v -> donationsDialog.donate(position));

            return convertView;
        }

        private static void strikeThrough(TextView textView, boolean strikeThrough) {
            textView.setPaintFlags(strikeThrough ? textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG : textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        static class ViewHolder {
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.text)
            TextView text;
            @BindView(R.id.price)
            TextView price;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
