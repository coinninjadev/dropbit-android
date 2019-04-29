package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.view.adapter.RestoreWalletPageAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class RestoreWalletActivityTest {

    private ShadowActivity shadowActivity;
    private RestoreWalletActivity activity;
    private RestoreWalletPageAdapter adapter;
    private Analytics mockAnalytics;

    private ViewPager mockViewPager() {
        ViewPager viewPager = mock(ViewPager.class);
        adapter = mock(RestoreWalletPageAdapter.class);
        when(viewPager.getAdapter()).thenReturn(adapter);
        activity.setViewPager(viewPager);
        return viewPager;
    }

    @Before
    public void setUp() {
        ActivityController<RestoreWalletActivity> activityController = Robolectric.buildActivity(RestoreWalletActivity.class);
        activity = activityController.get();
        shadowActivity = shadowOf(activity);
        activityController.create();
        ((ViewPager) activity.findViewById(R.id.recovery_words_pager)).setAdapter(mock(RestoreWalletPageAdapter.class));
        activityController.start().resume().visible();
        mockAnalytics = activity.analytics;
    }

    @Test
    public void navigating_back_on_page_two_finishes_activity() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(1);

        activity.onBackPressed();

        verify(viewPager).setCurrentItem(0);
        assertFalse(shadowActivity.isFinishing());
    }


    @Test
    public void navigating_back_on_page_one_finishes_activity() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(0);

        activity.onBackPressed();

        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void paging_past_word_limit_recovers_wallet() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(11);
        Button button = new Button(activity);
        button.setText("APPLE");

        activity.onPageForward(button);

        verify(viewPager, times(0)).setCurrentItem(anyInt());
        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getComponent().getClassName(),
                equalTo(CreatePinActivity.class.getName()));
        Bundle next_bundle = new Bundle();
        next_bundle.putStringArray(DropbitIntents.EXTRA_RECOVERY_WORDS, activity.recovery_words);
        assertThat(intent.getBundleExtra(DropbitIntents.EXTRA_NEXT_BUNDLE).getStringArray(DropbitIntents.EXTRA_RECOVERY_WORDS),
                equalTo(activity.recovery_words));
        assertThat(intent.getStringExtra(DropbitIntents.EXTRA_NEXT), equalTo(RecoverWalletActivity.class.getName()));
        assertThat(activity.recovery_words[11], equalTo("apple"));
        verify(mockAnalytics).trackEvent(Analytics.EVENT_WALLET_RESTORE);
    }

    @Test
    public void paging_forward_saves_recovery_word_in_lowercase() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(10);
        Button button = new Button(activity);
        button.setText("SATOSHI");

        activity.onPageForward(button);

        assertThat(activity.recovery_words[10], equalTo("satoshi"));
    }

    @Test
    public void instructs_view_to_reset_page_on_forward() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(1);
        Button button = new Button(activity);

        activity.onPageForward(button);

        verify(adapter).resetState();

    }

    @Test
    public void paging_forward_listener_tells_view_pager_to_advance() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(1);
        Button button = new Button(activity);

        activity.onPageForward(button);

        verify(viewPager).setCurrentItem(2);
    }

    @Test
    public void instructs_view_to_reset_page_on_back() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(1);

        activity.onPageBack(null);

        verify(adapter).resetState();
    }

    @Test
    public void clears_recovery_word_on_back() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(1);
        activity.recovery_words[1] = "word";

        activity.onPageBack(null);

        assertNull(activity.recovery_words[1]);
    }

    @Test
    public void will_not_go_back_below_0() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(0);

        activity.onPageBack(null);

        verify(viewPager, times(0)).setCurrentItem(anyInt());
    }

    @Test
    public void navigate_back_a_page_when_On_prevous_page_selected() {
        ViewPager viewPager = mockViewPager();
        when(viewPager.getCurrentItem()).thenReturn(2);

        activity.onPageBack(null);

        verify(viewPager).setCurrentItem(1);
    }

    @Test
    public void prepares_pageview_with_adapter() {
        assertNotNull(((ViewPager) activity.findViewById(R.id.recovery_words_pager))
                .getAdapter());
    }

    @Test
    public void instructed_to_restore_wallet() {
        assertThat(activity.getTitle().toString(), equalTo(activity.getString(R.string.restore_wallet_header)));
        assertThat(((TextView) activity.findViewById(R.id.headline)).getText().toString(),
                equalTo(activity.getString(R.string.restore_wallet_subtitle)));
    }
}