package com.coinninja.coinkeeper.model;

import com.coinninja.coinkeeper.R;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TrainingModelTest {

    @Test
    public void WHATS_BITCOIN_test() {
        TrainingModel trainingModel = TrainingModel.WHATS_BITCOIN;


        assertThat(trainingModel.getResVideoId(), equalTo(R.raw.dropbit_page_1_840x990));
        assertThat(trainingModel.getrVideoHeader(), equalTo(R.string.training_video_header_dropbit));
        assertThat(trainingModel.getrBodyHeader(), equalTo(R.string.training_body_header_whats_bitcoin));
        assertThat(trainingModel.getrBody(), equalTo(R.string.training_body_whats_bitcoin));
        assertThat(trainingModel.getrBodySubText(), equalTo(-1));
        assertThat(trainingModel.getrLearnLink(), equalTo(R.string.training_footer_learn_whats_bitcoin));
        assertThat(trainingModel.hasBodySubText(), equalTo(false));
        assertThat(trainingModel.hasVideo(), equalTo(true));
        assertThat(trainingModel.hasVideoHeader(), equalTo(true));
    }

    @Test
    public void SYSTEM_BROKEN_test() {
        TrainingModel trainingModel = TrainingModel.SYSTEM_BROKEN;


        assertThat(trainingModel.getResVideoId(), equalTo(R.raw.dropbit_page_2_840x990));
        assertThat(trainingModel.getrVideoHeader(), equalTo(R.string.training_video_header_dropbit));
        assertThat(trainingModel.getrBodyHeader(), equalTo(R.string.training_body_header_system_broken));
        assertThat(trainingModel.getrBody(), equalTo(R.string.training_body_system_broken));
        assertThat(trainingModel.getrBodySubText(), equalTo(-1));
        assertThat(trainingModel.getrLearnLink(), equalTo(R.string.training_footer_learn_system_broken));
        assertThat(trainingModel.hasBodySubText(), equalTo(false));
        assertThat(trainingModel.hasVideo(), equalTo(true));
        assertThat(trainingModel.hasVideoHeader(), equalTo(true));
    }

    @Test
    public void RECOVERY_WORDS_test() {
        TrainingModel trainingModel = TrainingModel.RECOVERY_WORDS;


        assertThat(trainingModel.getResVideoId(), equalTo(R.raw.dropbit_page_3_840x990));
        assertThat(trainingModel.getrVideoHeader(), equalTo(-1));
        assertThat(trainingModel.getrBodyHeader(), equalTo(R.string.training_body_header_recovery_words));
        assertThat(trainingModel.getrBody(), equalTo(R.string.training_body_recovery_words));
        assertThat(trainingModel.getrBodySubText(), equalTo(-1));
        assertThat(trainingModel.getrLearnLink(), equalTo(R.string.training_footer_learn_recovery_words));
        assertThat(trainingModel.hasBodySubText(), equalTo(false));
        assertThat(trainingModel.hasVideo(), equalTo(true));
        assertThat(trainingModel.hasVideoHeader(), equalTo(false));
    }

    @Test
    public void DROPBIT_test() {
        TrainingModel trainingModel = TrainingModel.DROPBIT;


        assertThat(trainingModel.getResVideoId(), equalTo(-1));
        assertThat(trainingModel.getrVideoHeader(), equalTo(-1));
        assertThat(trainingModel.getrBodyHeader(), equalTo(R.string.training_body_header_dropbit));
        assertThat(trainingModel.getrBody(), equalTo(R.string.training_body_dropbit));
        assertThat(trainingModel.getrBodySubText(), equalTo(R.string.training_body_subtext));
        assertThat(trainingModel.getrLearnLink(), equalTo(R.string.training_footer_learn_dropbit));
        assertThat(trainingModel.hasBodySubText(), equalTo(true));
        assertThat(trainingModel.hasVideo(), equalTo(false));
        assertThat(trainingModel.hasVideoHeader(), equalTo(false));
    }
}