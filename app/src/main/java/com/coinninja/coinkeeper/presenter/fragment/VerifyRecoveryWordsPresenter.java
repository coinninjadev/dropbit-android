package com.coinninja.coinkeeper.presenter.fragment;

import android.content.Context;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.Shuffler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class VerifyRecoveryWordsPresenter {
    private static final int ATTEMPT_LIMIT = 3;
    private static final int MAX_CHOICES = 5;

    private CNWalletManager cnWalletManager;
    private List<String> originalRecoveryWords;
    private int totalChallenges;
    private int numCompletedChallenges = 0;
    private Shuffler shuffler;
    private View view;
    private List<String> recoveryWords;
    private int wordIndex;
    private int attemptCounter = 0;
    private List<String> exludes = new ArrayList<>();

    @Inject
    public VerifyRecoveryWordsPresenter(CNWalletManager cnWalletManager, Shuffler shuffler) {
        this.cnWalletManager = cnWalletManager;
        this.shuffler = shuffler;
    }

    public void attach(View view, String[] recoveryWords, int totalChallenges) {
        originalRecoveryWords = Arrays.asList(recoveryWords);
        this.totalChallenges = totalChallenges;
        this.view = view;
    }

    public VerifyRecoveryWordsModel startNewChallenge() {
        recoveryWords = new ArrayList<>(originalRecoveryWords);
        attemptCounter = 0;
        return pick();
    }

    public VerifyRecoveryWordsModel pick() {
        for (String word : exludes) {
            recoveryWords.remove(word);
        }

        shuffler.shuffle(recoveryWords);
        String word = recoveryWords.get(shuffler.pick(MAX_CHOICES));
        wordIndex = originalRecoveryWords.indexOf(word);
        return new VerifyRecoveryWordsModel(wordIndex + 1, recoveryWords.subList(0, MAX_CHOICES));
    }

    public boolean onSelection(String word) {
        boolean isValid = originalRecoveryWords.indexOf(word) == wordIndex;
        if (!isValid & attemptCounter == (ATTEMPT_LIMIT - 1)) {
            throw new ChoiceSelectionLimitExceeded();
        } else if (!isValid) {
            attemptCounter += 1;
        }

        return isValid;
    }

    public int getAttemptCounter() {
        return attemptCounter;
    }

    public void onShowRecoveryWordsSelected() {
        view.showRecoveryWords();
    }

    public void onShowNextChallenge(String selection) {
        numCompletedChallenges += 1;
        if (numCompletedChallenges == totalChallenges) {
            completeChallege();
        } else {
            exludes.add(selection);
            view.showNextChallenge();
        }
    }


    public void excludeChoice(String choice) {
        exludes.add(choice);
    }

    public List<String> getExcludedChoices() {
        return exludes;
    }

    protected void completeChallege() {
        cnWalletManager.userVerifiedWords((String[]) originalRecoveryWords.toArray());
        view.onChallengeCompleted();
    }


    public static class ChoiceSelectionLimitExceeded extends RuntimeException {
    }

    public interface View {
        void showRecoveryWords();

        void showNextChallenge();

        void onChallengeCompleted();

        Context getContext();
    }
}

