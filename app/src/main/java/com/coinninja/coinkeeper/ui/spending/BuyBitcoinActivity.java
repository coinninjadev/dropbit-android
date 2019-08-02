package com.coinninja.coinkeeper.ui.spending;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.ui.util.CallbackHandler;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.android.LocationUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import java.util.HashMap;

import javax.inject.Inject;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.TYPE;

public class BuyBitcoinActivity extends BaseActivity {
    public static final String COPY_BITCOIN_DIALOG_TAG = "COPY_BITCOIN_ADDRESS";

    @Inject
    UserPreferences userPreferences;

    @Inject
    AccountManager accountManager;

    @Inject
    ClipboardUtil clipboardUtil;

    @Inject
    ActivityNavigationUtil activityNavigationUtil;

    @Inject
    LocationUtil locationUtil;

    private String receiveAddress;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (locationUtil.hasGrantedPermission(requestCode, permissions, grantResults)) {
            onNavigateToMap(locationUtil.getLastKnownLocation());
        } else {
            onNavigateToMap(null);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_bitcoin);
        receiveAddress = accountManager.getNextReceiveAddress();
    }

    @Override
    protected void onStart() {
        super.onStart();
        withId(this, R.id.buy_with_credit_card)
                .setOnClickListener(v -> setupNavigationToByBitcoinWithCreditCard(this));
        withId(this, R.id.buy_with_gift_card)
                .setOnClickListener(v -> setupNavigationToByBitcoinWithGiftCard(this));
        withId(this, R.id.buy_at_atm).setOnClickListener(v -> onBuyAtAtmClicked());
    }

    private void showNotice(CallbackHandler callbackHandler) {
        View alertView = getLayoutInflater().inflate(R.layout.dialog_copy_bitcoin_address, null);
        GenericAlertDialog.newInstance(
                alertView,
                false,
                false
        ).show(getSupportFragmentManager(), COPY_BITCOIN_DIALOG_TAG);
        alertView.findViewById(R.id.address_button).setOnClickListener(v -> copyBitcoinAddress(true));
        alertView.findViewById(R.id.close).setOnClickListener(v -> dismissCurrentNotice());
        ((Button) alertView.findViewById(R.id.address_button)).setText(receiveAddress);
        alertView.findViewById(R.id.ok).setOnClickListener(v -> navigateToRouteWithAddress(callbackHandler));
    }

    private void setupNavigationToByBitcoinWithCreditCard(Activity activity) {
        CallbackHandler handler = () -> activityNavigationUtil.navigateToBuyBitcoinWithCreditCard(activity);
        showCopyUI(handler);
    }

    private void setupNavigationToByBitcoinWithGiftCard(Activity activity) {
        CallbackHandler handler = () -> activityNavigationUtil.navigateToBuyBitcoinWithGiftCard(activity);
        showCopyUI(handler);
    }

    private void showCopyUI(CallbackHandler handler) {
        showNotice(handler);
    }

    private void dismissCurrentNotice() {
        GenericAlertDialog dialog = (GenericAlertDialog) getSupportFragmentManager().findFragmentByTag(COPY_BITCOIN_DIALOG_TAG);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void navigateToRouteWithAddress(CallbackHandler callbackHandler) {
        copyBitcoinAddress(false);
        callbackHandler.callback();
    }

    private void copyBitcoinAddress(boolean showNotice) {
        clipboardUtil.setClipFromText("Bitcoin Address", receiveAddress);
        if (showNotice) {
            Toast.makeText(this,
                    getString(R.string.bitcoin_address_copy),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void onBuyAtAtmClicked() {
        if (locationUtil.canReadLocation()) {
            onNavigateToMap(locationUtil.getLastKnownLocation());
        } else {
            locationUtil.requestPermissionToAccessLocationFor(this);
        }
    }

    private void onNavigateToMap(@Nullable Location location) {
        HashMap<CoinNinjaParameter, String> parameters = new HashMap<>();
        parameters.put(TYPE, "atms");
        String event = Analytics.Companion.EVENT_BUY_BITCOIN_AT_ATM;
        activityNavigationUtil.navigatesToMapWith(this, parameters, location, event);
    }
}
