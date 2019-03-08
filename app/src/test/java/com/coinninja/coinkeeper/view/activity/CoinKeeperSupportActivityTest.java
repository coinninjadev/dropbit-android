package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.net.Uri;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import androidx.recyclerview.widget.RecyclerView;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CoinKeeperSupportActivityTest {

    private ActivityController<CoinKeeperSupportActivity> activityController;
    private CoinKeeperSupportActivity activity;
    private ShadowActivity shadowActivity;

    @Before
    public void setUp() {
        activityController = Robolectric.buildActivity(CoinKeeperSupportActivity.class);
        activity = activityController.get();
        shadowActivity = shadowOf(activity);
        activityController.create().resume().start().visible();
    }

    @Test
    public void goes_to_uri_when_item_selected() {
        activity.onItemSelected(Uri.parse("https://example.com"));

        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getAction(), equalTo(Intent.ACTION_VIEW));
        assertThat(intent.getData().toString(), equalTo("https://example.com"));
    }

    @Test
    public void sets_adapter_on_recycler_view() {
        assertNotNull(((RecyclerView) activity.findViewById(R.id.list)).getAdapter());
    }
}