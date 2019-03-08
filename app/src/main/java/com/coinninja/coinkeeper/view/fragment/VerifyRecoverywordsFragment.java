package com.coinninja.coinkeeper.view.fragment;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsModel;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsPresenter;
import com.coinninja.coinkeeper.ui.base.BaseFragment;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;

public class VerifyRecoverywordsFragment extends BaseFragment implements View.OnClickListener, DialogInterface.OnClickListener {

    protected final static String TAG_WRONG_CHOICE_ERROR = "TAG_WRONG_CHOICE_ERROR";
    protected final static String TAG_TOO_MANY_WRONG_CHOICES_ERROR = "TAG_TOO_MANY_WRONG_CHOICES_ERROR";
    private VerifyRecoveryWordsPresenter presenter;
    private VerifyRecoveryWordsModel recoveryWordsModel;
    private View view;
    private State cuurentState = State.VERIFING;

    public static VerifyRecoverywordsFragment newInstance(VerifyRecoveryWordsPresenter presenter) {
        VerifyRecoverywordsFragment fragment = new VerifyRecoverywordsFragment();
        fragment.setPresenter(presenter);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.verify_recovery_words, container, false);
        recoveryWordsModel = presenter.startNewChallenge();
        presentChallenge();
        return view;
    }

    public void presentChallenge() {
        List<String> choices = recoveryWordsModel.getChoices();
        ((TextView) view.findViewById(R.id.which_word)).
                setText(Integer.toString(recoveryWordsModel.getWordNumber()));

        ((Button) view.findViewById(R.id.challenge_1)).setText(choices.get(0).toUpperCase(Locale.ENGLISH));
        (view.findViewById(R.id.challenge_1)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.challenge_2)).setText(choices.get(1).toUpperCase(Locale.ENGLISH));
        (view.findViewById(R.id.challenge_2)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.challenge_3)).setText(choices.get(2).toUpperCase(Locale.ENGLISH));
        (view.findViewById(R.id.challenge_3)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.challenge_4)).setText(choices.get(3).toUpperCase(Locale.ENGLISH));
        (view.findViewById(R.id.challenge_4)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.challenge_5)).setText(choices.get(4).toUpperCase(Locale.ENGLISH));
        (view.findViewById(R.id.challenge_5)).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        try {

            String selection = ((Button) view).getText().toString().toLowerCase(Locale.ENGLISH);

            if (presenter.onSelection(selection)) {
                presenter.onShowNextChallenge(selection);
            } else {
                cuurentState = State.WRONG_CHOICE;
                GenericAlertDialog.newInstance(null, getString(R.string.verify_recovery_words_fail_message),
                        getString(R.string.verify_recovery_words_fail_positive_button),
                        getString(R.string.verify_recovery_words_fail_negative_button),
                        this, false, false).show(getFragmentManager(), TAG_WRONG_CHOICE_ERROR);
            }
        } catch (VerifyRecoveryWordsPresenter.ChoiceSelectionLimitExceeded e) {
            cuurentState = State.TOO_MANY_ERRORS;
            GenericAlertDialog.newInstance(null,
                    getString(R.string.verify_recovery_words_error_message),
                    getString(R.string.verify_recovery_words_error_positive_button),
                    null,
                    this, false, false).show(getFragmentManager(), TAG_TOO_MANY_WRONG_CHOICES_ERROR);

        }
    }

    public void setPresenter(VerifyRecoveryWordsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (cuurentState == State.WRONG_CHOICE && which == Dialog.BUTTON_NEGATIVE) {
            presenter.onShowRecoveryWordsSelected();
        } else if (cuurentState == State.TOO_MANY_ERRORS) {
            presenter.onShowRecoveryWordsSelected();
        }

        dialog.dismiss();

    }

    enum State {
        VERIFING, WRONG_CHOICE, TOO_MANY_ERRORS
    }

}
