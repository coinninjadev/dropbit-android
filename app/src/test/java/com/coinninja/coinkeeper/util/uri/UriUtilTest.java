package com.coinninja.coinkeeper.util.uri;

import android.content.Intent;
import android.net.Uri;

import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.matchers.ActivityMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.coinkeeper.util.uri.routes.DropbitRoute.DROPBIT_TRANSACTION;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class UriUtilTest {

    TestableActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(TestableActivity.class);

    }

    @After
    public void tearDown() throws Exception {
        activity = null;
    }

    @Test
    public void test_navigates_to_uri() {
        DropbitUriBuilder builder = new DropbitUriBuilder();
        Uri uri = builder.build(DROPBIT_TRANSACTION);

        UriUtil.openUrl(uri, activity);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
    }

}
