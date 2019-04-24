package com.coinninja.coinkeeper.view.widget;

import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.TestableActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DefaultCurrencyDisplaySyncViewTest {

    private DefaultCurrencyDisplaySyncView defaultCurrencyDisplayView;

    @Before
    public void setUp() {
        TestableActivity activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.appbar_layout);
        defaultCurrencyDisplayView = withId(activity, R.id.balance);
    }

    @Test
    public void updates_syncing_ui_correctly_when_shown() {
        defaultCurrencyDisplayView.showSyncingUI();
        assertThat(withId(defaultCurrencyDisplayView, R.id.syncing_image).getVisibility(), equalTo(View.VISIBLE));
        assertThat(withId(defaultCurrencyDisplayView, R.id.syncing_text).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void updates_syncing_ui_correctly_when_hiding() {
        defaultCurrencyDisplayView.hideSyncingUI();
        assertThat(withId(defaultCurrencyDisplayView, R.id.syncing_image).getVisibility(), equalTo(View.GONE));
        assertThat(withId(defaultCurrencyDisplayView, R.id.syncing_text).getVisibility(), equalTo(View.GONE));
    }

}