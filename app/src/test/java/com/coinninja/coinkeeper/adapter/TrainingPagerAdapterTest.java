package com.coinninja.coinkeeper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.TrainingModel;
import com.coinninja.coinkeeper.view.activity.TrainingActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class TrainingPagerAdapterTest {
    private TestCoinKeeperApplication application;
    private TrainingPagerAdapter trainingPagerAdapter;
    private TrainingActivity activity;

    @Before
    public void setUp() throws Exception {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        ActivityController<TrainingActivity> trainingActivityController = Robolectric.buildActivity(TrainingActivity.class);
        activity = trainingActivityController.get();
        trainingActivityController.create().resume().start();

        trainingPagerAdapter = new TrainingPagerAdapter(activity, activity);
    }

    @After
    public void tearDown() throws Exception {
        application = null;
        trainingPagerAdapter = null;
        activity = null;
    }

    @Test
    public void bind_video_view_WHATS_BITCOIN_test() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_training, activity.findViewById(R.id.training_pager), false);
        LinearLayout dropbitLayout = layout.findViewById(R.id.training_drop_layout);
        LinearLayout videoLayout = layout.findViewById(R.id.video_layout);
        VideoView videoView = layout.findViewById(R.id.videoView);
        TextView videoHeader = layout.findViewById(R.id.video_message);
        TextView bodyHeaderText = layout.findViewById(R.id.training_body_header);
        TextView bodyText = layout.findViewById(R.id.training_body);
        TextView bodySubText = layout.findViewById(R.id.training_body_subtext);


        TrainingModel trainingModel = TrainingModel.WHATS_BITCOIN;

        trainingPagerAdapter.bindVideo(dropbitLayout, videoLayout, videoView, videoHeader, trainingModel);


        assertThat(dropbitLayout.getVisibility(), equalTo(View.GONE));
        assertThat(videoLayout.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void assert_that_when_binding_a_video_that_the_video_is_added_to_the_list_of_restart_able_videos_test() {
        TrainingModel trainingModel = TrainingModel.WHATS_BITCOIN;
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_training, activity.findViewById(R.id.training_pager), false);
        LinearLayout dropbitLayout = layout.findViewById(R.id.training_drop_layout);
        LinearLayout videoLayout = layout.findViewById(R.id.video_layout);
        VideoView videoView = layout.findViewById(R.id.videoView);
        TextView videoHeader = layout.findViewById(R.id.video_message);

        trainingPagerAdapter.bindVideo(dropbitLayout, videoLayout, videoView, videoHeader, trainingModel);


        assertThat(trainingPagerAdapter.videoHolder.contains(videoView), equalTo(true));
    }

    @Test
    public void bind_video_view_RECOVERY_WORDS_test() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_training, activity.findViewById(R.id.training_pager), false);
        LinearLayout dropbitLayout = layout.findViewById(R.id.training_drop_layout);
        LinearLayout videoLayout = layout.findViewById(R.id.video_layout);
        VideoView videoView = layout.findViewById(R.id.videoView);
        TextView videoHeader = layout.findViewById(R.id.video_message);


        TrainingModel trainingModel = TrainingModel.RECOVERY_WORDS;

        trainingPagerAdapter.bindVideo(dropbitLayout, videoLayout, videoView, videoHeader, trainingModel);


        assertThat(dropbitLayout.getVisibility(), equalTo(View.GONE));
        assertThat(videoLayout.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void bind_body_test() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_training, activity.findViewById(R.id.training_pager), false);
        TextView bodyText = layout.findViewById(R.id.training_body);

        TrainingModel trainingModel = TrainingModel.WHATS_BITCOIN;

        trainingPagerAdapter.bindBody(bodyText, trainingModel);


        assertThat(bodyText.getText(), equalTo("Bitcoin is a currency that’s completely owned and controlled by you. It can be sent anywhere in the world, very quickly, cheaply and without the need for a 3rd party transmitter, like a bank."));
    }

    @Test
    public void bind_body_header_test() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_training, activity.findViewById(R.id.training_pager), false);
        TextView bodyHeaderText = layout.findViewById(R.id.training_body_header);

        TrainingModel trainingModel = TrainingModel.WHATS_BITCOIN;

        trainingPagerAdapter.bindBodyHeader(bodyHeaderText, trainingModel);


        assertThat(bodyHeaderText.getText(), equalTo("What is Bitcoin?"));
    }

    @Test
    public void bind_body_subtext_test() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_training, activity.findViewById(R.id.training_pager), false);
        TextView bodySubText = layout.findViewById(R.id.training_body_subtext);

        TrainingModel trainingModel = TrainingModel.WHATS_BITCOIN;

        trainingPagerAdapter.bindBodySubText(bodySubText, trainingModel);


        assertThat(bodySubText.getText(), equalTo("*You must verify your number if you are accepting Bitcoin from an SMS invite"));
    }


    @Test
    public void bind_body_subtext_RECOVERY_WORDS_test() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_training, activity.findViewById(R.id.training_pager), false);
        TextView bodySubText = layout.findViewById(R.id.training_body_subtext);

        TrainingModel trainingModel = TrainingModel.RECOVERY_WORDS;

        trainingPagerAdapter.bindBodySubText(bodySubText, trainingModel);


        assertThat(bodySubText.getText(), equalTo("*You must verify your number if you are accepting Bitcoin from an SMS invite"));
    }


    @Test
    public void bind_video_header_test() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_training, activity.findViewById(R.id.training_pager), false);
        TextView videoHeader = layout.findViewById(R.id.video_message);

        TrainingModel trainingModel = TrainingModel.WHATS_BITCOIN;

        trainingPagerAdapter.bindVideoHeader(videoHeader, trainingModel);


        assertThat(videoHeader.getText(), equalTo("I know about Bitcoin…skip"));
    }

    @Test
    public void restart_all_videos_on_restart_test() {
        VideoView videoView1 = mock(VideoView.class);
        VideoView videoView2 = mock(VideoView.class);
        VideoView videoView3 = mock(VideoView.class);
        trainingPagerAdapter.addVideoToHolder(videoView1);
        trainingPagerAdapter.addVideoToHolder(videoView2);
        trainingPagerAdapter.addVideoToHolder(videoView3);


        trainingPagerAdapter.restartAllVideos();


        verify(videoView1, times(1)).setVisibility(View.VISIBLE);
        verify(videoView1, times(1)).start();
        verify(videoView1, times(1)).seekTo(0);
        verify(videoView2, times(1)).setVisibility(View.VISIBLE);
        verify(videoView2, times(1)).start();
        verify(videoView2, times(1)).seekTo(0);
        verify(videoView3, times(1)).setVisibility(View.VISIBLE);
        verify(videoView3, times(1)).start();
        verify(videoView3, times(1)).seekTo(0);
    }

    @Test
    public void no_not_add_duplicate_videos_to_video_holder() {
        VideoView videoView1 = mock(VideoView.class);

        trainingPagerAdapter.addVideoToHolder(videoView1);
        trainingPagerAdapter.addVideoToHolder(videoView1);
        trainingPagerAdapter.addVideoToHolder(videoView1);
        trainingPagerAdapter.addVideoToHolder(videoView1);
        trainingPagerAdapter.addVideoToHolder(videoView1);
        trainingPagerAdapter.addVideoToHolder(videoView1);


        assertThat(trainingPagerAdapter.videoHolder.size(), equalTo(1));
    }
}