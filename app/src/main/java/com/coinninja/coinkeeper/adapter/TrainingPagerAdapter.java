package com.coinninja.coinkeeper.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.TrainingModel;

import java.util.ArrayList;

import androidx.viewpager.widget.PagerAdapter;

public class TrainingPagerAdapter extends PagerAdapter {

    private final Context context;
    private final OnTrainingClickListener onTrainingClickListener;
    protected ArrayList<VideoView> videoHolder = new ArrayList<>();

    public TrainingPagerAdapter(Context context, OnTrainingClickListener onTrainingClickListener) {
        this.context = context;
        this.onTrainingClickListener = onTrainingClickListener;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        TrainingModel trainingModel = TrainingModel.values()[position];
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_training, collection, false);
        bindToLayout(layout, trainingModel);
        collection.addView(layout);
        return layout;
    }

    private void bindToLayout(ViewGroup layout, TrainingModel trainingModel) {
        LinearLayout dropbitLayout = layout.findViewById(R.id.training_drop_layout);
        LinearLayout videoLayout = layout.findViewById(R.id.video_layout);
        VideoView videoView = layout.findViewById(R.id.videoView);
        TextView videoHeader = layout.findViewById(R.id.video_message);
        TextView bodyHeaderText = layout.findViewById(R.id.training_body_header);
        TextView bodyText = layout.findViewById(R.id.training_body);
        TextView bodySubText = layout.findViewById(R.id.training_body_subtext);

        bindVideo(dropbitLayout, videoLayout, videoView, videoHeader, trainingModel);
        bindVideoHeader(videoHeader, trainingModel);
        bindBodyHeader(bodyHeaderText, trainingModel);
        bindBody(bodyText, trainingModel);
        bindBodySubText(bodySubText, trainingModel);
    }

    protected void bindVideo(LinearLayout dropbitLayout, LinearLayout videoLayout, VideoView videoView, TextView videoHeader, TrainingModel trainingModel) {
        if (trainingModel.hasVideo()) {
            dropbitLayout.setVisibility(View.GONE);
            videoLayout.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + trainingModel.getrVideoId());
            videoView.setVideoURI(uri);
            restartVideo(videoView);
            addVideoToHolder(videoView);
        } else {
            dropbitLayout.setVisibility(View.VISIBLE);
            videoLayout.setVisibility(View.GONE);
        }
    }

    protected void bindVideoHeader(TextView videoHeader, TrainingModel trainingModel) {
        if (trainingModel.hasVideoHeader()) {
            videoHeader.setText(context.getText(trainingModel.getrVideoHeader()));
            videoHeader.setOnClickListener(view -> onTrainingClickListener.onSkipClicked(trainingModel));
        } else {
            videoHeader.setText("");
            videoHeader.setOnClickListener(null);
        }
    }

    protected void bindBodyHeader(TextView bodyHeaderText, TrainingModel trainingModel) {
        bodyHeaderText.setText(context.getText(trainingModel.getrBodyHeader()));
    }

    protected void bindBody(TextView bodyText, TrainingModel trainingModel) {
        bodyText.setText(context.getText(trainingModel.getrBody()));
    }

    protected void bindBodySubText(TextView bodySubText, TrainingModel trainingModel) {
        if (trainingModel.hasBodySubText()) {
            bodySubText.setVisibility(View.VISIBLE);
        } else {
            bodySubText.setVisibility(View.GONE);
        }
    }


    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return TrainingModel.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    protected void addVideoToHolder(VideoView videoView) {
        if (!videoHolder.contains(videoView)) {
            videoHolder.add(videoView);
        }
    }

    public void restartAllVideos() {
        for (VideoView videoView : videoHolder) {
            restartVideo(videoView);
        }
    }

    protected void restartVideo(VideoView videoView) {
        videoView.setVisibility(View.VISIBLE);
        videoView.start();
        videoView.seekTo(0);
    }

    public interface OnTrainingClickListener {
        void onLearnLinkClicked(TrainingModel trainingModel);

        void onEndActionButtonClicked(TrainingModel trainingModel);

        void onSkipClicked(TrainingModel trainingModel);
    }
}