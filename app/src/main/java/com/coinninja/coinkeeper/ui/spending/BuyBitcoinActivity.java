package com.coinninja.coinkeeper.ui.spending;

import android.location.Location;
import android.os.Bundle;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocationUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter;

import java.util.HashMap;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.TYPE;

public class BuyBitcoinActivity extends BaseActivity {
    @Inject
    ActivityNavigationUtil activityNavigationUtil;

    @Inject
    LocationUtil locationUtil;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        withId(this, R.id.buy_with_credit_card)
                .setOnClickListener(v -> activityNavigationUtil.navigateToBuyBitcoinWithCreditCard(this));
        withId(this, R.id.buy_with_gift_card)
                .setOnClickListener(v -> activityNavigationUtil.navigateToBuyBitcoinWithGiftCard(this));
        withId(this, R.id.buy_at_atm).setOnClickListener(v -> onBuyAtAtmClicked());
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
        String event = Analytics.EVENT_BUY_BITCOIN_AT_ATM;
        activityNavigationUtil.navigatesToMapWith(this, parameters, location, event);
    }
}
