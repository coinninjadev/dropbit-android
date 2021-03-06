package com.coinninja.coinkeeper.view.widget;

import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.TestableActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DefaultCurrencyDisplaySyncViewTest {

    private DefaultCurrencyDisplaySyncView defaultCurrencyDisplayView;

    @Before
    public void setUp() {
        TestableActivity activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.cn_base_layout);
        defaultCurrencyDisplayView = activity.findViewById(R.id.appbar_balance);
    }

    @Test
    public void updates_syncing_ui_correctly_when_shown() {
        defaultCurrencyDisplayView.showSyncingUI();
        assertThat(defaultCurrencyDisplayView.findViewById(R.id.syncing_image).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void updates_syncing_ui_correctly_when_hiding() {
        defaultCurrencyDisplayView.hideSyncingUI();
        assertThat(defaultCurrencyDisplayView.findViewById(R.id.syncing_image).getVisibility(), equalTo(View.GONE));
    }

}