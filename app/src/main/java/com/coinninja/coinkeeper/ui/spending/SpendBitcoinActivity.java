package com.coinninja.coinkeeper.ui.spending;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocationUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter;

import java.util.HashMap;

import javax.inject.Inject;

import static com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.TYPE;

public class SpendBitcoinActivity extends BaseActivity {

    @Inject
    ActivityNavigationUtil activityNavigationUtil;

    @Inject
    LocationUtil locationUtil;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (locationUtil.hasGrantedPermission(requestCode, permissions, grantResults)) {
            goToMapWebView(locationUtil.getLastKnownLocation());
        } else {
            goToMapWebView(null);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spend_bitcoin);
    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.buy_giftcard_button)
                .setOnClickListener(v -> activityNavigationUtil.navigateToBuyGiftCard(this));
        findViewById(R.id.online_button)
                .setOnClickListener(v -> activityNavigationUtil.navigateToWhereToSpend(this));
        findViewById(R.id.around_me_button).setOnClickListener(v -> onAroundMeButtonClicked());
    }


    private void goToMapWebView(Location location) {
        HashMap<CoinNinjaParameter, String> parameters = new HashMap<>();
        parameters.put(TYPE, "spend");
        String event = Analytics.Companion.EVENT_SPEND_AROUND_ME;
        activityNavigationUtil.navigatesToMapWith(this, parameters, location, event);
    }

    private void onAroundMeButtonClicked() {
        if (locationUtil.canReadLocation()) {
            goToMapWebView(locationUtil.getLastKnownLocation());
        } else {
            locationUtil.requestPermissionToAccessLocationFor(this);
        }
    }
}
