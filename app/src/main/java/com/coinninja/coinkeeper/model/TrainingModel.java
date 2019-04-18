package com.coinninja.coinkeeper.model;

import com.coinninja.coinkeeper.R;

public enum TrainingModel {


    WHATS_BITCOIN(
            R.raw.dropbit_page_1_840x990,
            R.string.training_body_header_whats_bitcoin,
            R.string.training_body_whats_bitcoin,
            -1,
            R.string.training_footer_learn_whats_bitcoin),
    SYSTEM_BROKEN(
            R.raw.dropbit_page_2_840x990,
            R.string.training_body_header_system_broken,
            R.string.training_body_system_broken,
            -1,
            R.string.training_footer_learn_system_broken),
    RECOVERY_WORDS(
            R.raw.dropbit_page_3_840x990,
            R.string.training_body_header_recovery_words,
            R.string.training_body_recovery_words,
            -1,
            R.string.training_footer_learn_recovery_words),
    DROPBIT(
            -1,
            R.string.training_body_header_dropbit,
            R.string.training_body_dropbit,
            R.string.training_body_subtext,
            R.string.training_footer_learn_dropbit);


    private final int rBodyHeader;
    private final int rBody;
    private final int rBodySubText;
    private final int rLearnLink;

    private int rVideoId;

    TrainingModel(int rVideoId, int rBodyHeader, int rBody, int rBodySubText, int rLearnLink) {
        this.rVideoId = rVideoId;
        this.rBodyHeader = rBodyHeader;
        this.rBody = rBody;
        this.rBodySubText = rBodySubText;
        this.rLearnLink = rLearnLink;
    }

    public int getResVideoId() {
        return rVideoId;
    }

    public int getrBodyHeader() {
        return rBodyHeader;
    }

    public int getrBody() {
        return rBody;
    }

    public int getrBodySubText() {
        return rBodySubText;
    }

    public int getrVideoId() {
        return rVideoId;
    }

    public int getrLearnLink() {
        return rLearnLink;
    }

    public boolean hasVideo() {
        return rVideoId > 0;
    }

    public boolean hasBodySubText() {
        return rBodySubText > 0;
    }

}
