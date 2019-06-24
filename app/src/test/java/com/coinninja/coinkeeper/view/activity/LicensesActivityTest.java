package com.coinninja.coinkeeper.view.activity;

import android.webkit.WebView;

import com.coinninja.coinkeeper.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowWebView;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class LicensesActivityTest {

    private ActivityController<LicensesActivity> activityController;
    private LicensesActivity activity;

    @Before
    public void setUp() {
        activityController = Robolectric.buildActivity(LicensesActivity.class);
        activity = activityController.get();
        activityController.create().start().resume().visible();
    }

    @Test
    public void sets_url() {
        ShadowWebView shadowWebView = shadowOf((WebView) activity.findViewById(R.id.license_webview));
        String url = shadowWebView.getLastLoadedUrl();

        assertThat(url.toString(), equalTo("file:///android_asset/licensing.html"));
    }

    @Test
    public void renders_content() {
        assertNotNull(activity.findViewById(R.id.license_webview));
    }

}