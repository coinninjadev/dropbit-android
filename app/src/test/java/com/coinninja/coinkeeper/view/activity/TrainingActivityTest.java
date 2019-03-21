package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.adapter.TrainingPagerAdapter;
import com.coinninja.coinkeeper.model.TrainingModel;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.google.android.material.tabs.TabLayout;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class TrainingActivityTest {

    @Mock
    UserHelper userHelper;

    @Mock
    ActivityNavigationUtil activityNavigationUtil;

    private ActivityController<TrainingActivity> activityActivityController;
    private TrainingActivity activity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        activityActivityController = Robolectric.buildActivity(TrainingActivity.class);
        activity = activityActivityController.get();
        activity.userHelper = userHelper;
        activity.activityNavigationUtil = activityNavigationUtil;
    }

    @After
    public void tearDown() {
        userHelper = null;
        activityActivityController = null;
        activityNavigationUtil = null;
        activity = null;
    }

    @Test
    public void init_pager() {
        start();
        TrainingPagerAdapter mockAdapter = mock(TrainingPagerAdapter.class);

        activity.initPager(mockAdapter);

        assertThat(activity.viewPager.getAdapter(), equalTo(mockAdapter));
    }

    private void start() {
        activityActivityController.create().start().resume();
    }

    @Test
    public void init_first_page() {
        start();

        activity.initFirstPage();
        TextView learnLink = activity.findViewById(R.id.training_footer_learn_link);
        TabLayout tabLayout = activity.findViewById(R.id.training_footer_dots);
        Button button = activity.findViewById(R.id.training_footer_action_button);


        assertThat(learnLink.getText().toString(), equalTo("Learn more about Bitcoin"));
        assertThat(tabLayout.getVisibility(), equalTo(View.VISIBLE));
        assertThat(button.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void click_learn_link_WHATS_BITCOIN_test() {
        start();
        ShadowActivity shadowActivity = shadowOf(activity);
        TrainingModel trainingModel = TrainingModel.WHATS_BITCOIN;

        activity.onLearnLinkClicked(trainingModel);


        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getAction(), equalTo(Intent.ACTION_VIEW));
        assertThat(nextStartedActivity.getData().toString(),
                equalTo("https://coinninja.com/learnbitcoin"));
    }

    @Test
    public void click_learn_link_DROPBIT_test() {
        start();
        ShadowActivity shadowActivity = shadowOf(activity);
        TrainingModel trainingModel = TrainingModel.DROPBIT;

        activity.onLearnLinkClicked(trainingModel);


        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getAction(), equalTo(Intent.ACTION_VIEW));
        assertThat(nextStartedActivity.getData().toString(),
                equalTo("https://dropbit.app/dropbit"));
    }

    @Test
    public void click_learn_link_RECOVERY_WORDS_test() {
        start();
        ShadowActivity shadowActivity = shadowOf(activity);
        TrainingModel trainingModel = TrainingModel.RECOVERY_WORDS;

        activity.onLearnLinkClicked(trainingModel);


        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getAction(), equalTo(Intent.ACTION_VIEW));
        assertThat(nextStartedActivity.getData().toString(),
                equalTo("https://coinninja.com/recoverywords"));
    }

    @Test
    public void click_learn_link_SYSTEM_BROKEN_test() {
        start();
        ShadowActivity shadowActivity = shadowOf(activity);
        TrainingModel trainingModel = TrainingModel.SYSTEM_BROKEN;

        activity.onLearnLinkClicked(trainingModel);


        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getAction(), equalTo(Intent.ACTION_VIEW));
        assertThat(nextStartedActivity.getData().toString(),
                equalTo("https://coinninja.com/whybitcoin"));
    }

    @Test
    public void restart_all_videos_on_resume_test() {
        TrainingPagerAdapter mockAdapter = mock(TrainingPagerAdapter.class);
        activityActivityController.create();
        activity.trainingAdapter = mockAdapter;

        activityActivityController.resume();

        verify(mockAdapter).restartAllVideos();
    }
}