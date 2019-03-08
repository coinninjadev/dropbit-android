package com.coinninja.coinkeeper.presenter.fragment;

import java.util.List;

public class VerifyRecoveryWordsModel {

    private int wordNumber;
    private List<String> choices;

    public VerifyRecoveryWordsModel(int wordNumber, List<String> choices) {
        this.wordNumber = wordNumber;
        this.choices = choices;
    }

    public int getWordNumber() {
        return wordNumber;
    }

    public List<String> getChoices() {
        return choices;
    }
}
