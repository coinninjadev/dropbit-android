package com.coinninja.coinkeeper.view.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.widget.Button;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsModel;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsPresenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class VerifyRecoverywordsActivityFragmentTest {


    private final List<String> randomChoices = Arrays.asList("word12", "word6", "word8", "word1", "word10");

    private VerifyRecoveryWordsPresenter presenter;
    private VerifyRecoverywordsFragment fragment;
    private Resources resources;
    private VerifyRecoveryWordsModel recoveryWordsModel = new VerifyRecoveryWordsModel(2, randomChoices);


    @Before
    public void setupFragment() {
        presenter = mock(VerifyRecoveryWordsPresenter.class);
        when(presenter.startNewChallenge()).thenReturn(recoveryWordsModel);
        FragmentController<VerifyRecoverywordsFragment> fragmentController = Robolectric.buildFragment(VerifyRecoverywordsFragment.class);
        fragment = fragmentController.get();
        fragment.setPresenter(presenter);
        fragmentController.create().start().resume().visible();
        resources = fragment.getResources();
    }


    @Test
    public void itPopulatesWhichWordToLocate() {
        TextView which = fragment.getView().findViewById(R.id.which_word);
        assertThat(which.getText().toString(), equalTo("2"));
    }

    @Test
    public void itPopulatesFirstRandomWord() {
        Button choice = fragment.getView().findViewById(R.id.challenge_1);
        assertThat(choice.getText().toString(), equalTo("word12".toUpperCase(Locale.ENGLISH)));
        ShadowView shadow = shadowOf(choice);
        assertNotNull(shadow.getOnClickListener());
    }

    @Test
    public void itPopulatesSecondRandomWord() {
        Button choice = fragment.getView().findViewById(R.id.challenge_2);
        assertThat(choice.getText().toString(), equalTo("word6".toUpperCase(Locale.ENGLISH)));
        ShadowView shadow = shadowOf(choice);
        assertNotNull(shadow.getOnClickListener());
    }

    @Test
    public void itPopulatesThirdRandomWord() {
        Button choice = fragment.getView().findViewById(R.id.challenge_3);
        assertThat(choice.getText().toString(), equalTo("word8".toUpperCase(Locale.ENGLISH)));
        ShadowView shadow = shadowOf(choice);
        assertNotNull(shadow.getOnClickListener());
    }

    @Test
    public void itPopulatesFourthRandomWord() {
        Button choice = fragment.getView().findViewById(R.id.challenge_4);
        assertThat(choice.getText().toString(), equalTo("word1".toUpperCase(Locale.ENGLISH)));
        ShadowView shadow = shadowOf(choice);
        assertNotNull(shadow.getOnClickListener());
    }

    @Test
    public void itPopulatesFifthRandomWord() {
        Button choice = fragment.getView().findViewById(R.id.challenge_5);
        assertThat(choice.getText().toString(), equalTo("word10".toUpperCase(Locale.ENGLISH)));
        ShadowView shadow = shadowOf(choice);
        assertNotNull(shadow.getOnClickListener());
    }

    @Test
    public void selectingWrongWordAsksYouIfYouWantToTryAgain() {
        // Correct Choice: 2
        // Choices: ("word12", "word6", "word8", "word1", "word10")
        when(presenter.onSelection("word10")).thenReturn(false);

        fragment.getView().findViewById(R.id.challenge_5).performClick();

        Fragment badChoiceAlert = fragment.getFragmentManager().
                findFragmentByTag(VerifyRecoverywordsFragment.TAG_WRONG_CHOICE_ERROR);
        assertNotNull(badChoiceAlert);
    }

    @Test
    public void dialogGetsDismissedWhenUserClicksNegativeButton() {
        DialogInterface dialog = mock(DialogInterface.class);
        when(presenter.onSelection("word10")).thenReturn(false);
        fragment.getView().findViewById(R.id.challenge_5).performClick();

        fragment.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);

        verify(dialog, times(1)).dismiss();
    }

    @Test
    public void afterSelectingWrongChoiceYouCanReviewWordsAgain() {
        DialogInterface dialog = mock(DialogInterface.class);
        when(presenter.onSelection("word10")).thenReturn(false);
        fragment.getView().findViewById(R.id.challenge_5).performClick();

        fragment.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);

        verify(presenter, times(1)).onShowRecoveryWordsSelected();
    }

    @Test
    public void afterSelectingWrongWordYouCanTryAgain() {
        DialogInterface dialog = mock(DialogInterface.class);
        when(presenter.onSelection("word10")).thenReturn(false);
        fragment.getView().findViewById(R.id.challenge_5).performClick();

        fragment.onClick(dialog, DialogInterface.BUTTON_POSITIVE);

        verify(dialog, times(1)).dismiss();
    }

    @Test
    public void selectingCorrectWordTakesYouToNextWord() {
        // Correct Choice: 2
        // Choices: ("word12", "word6", "word8", "word1", "word10")
        when(presenter.onSelection("word1")).thenReturn(true);

        fragment.getView().findViewById(R.id.challenge_4).performClick();

        verify(presenter, times(1)).onShowNextChallenge("word1");
    }

    @Test
    public void selectingWrongWordToMannyTimesHasYouStartOver() {
        when(presenter.onSelection("word10")).thenThrow(new VerifyRecoveryWordsPresenter.ChoiceSelectionLimitExceeded());

        fragment.getView().findViewById(R.id.challenge_5).performClick();

        Fragment choiceLimitReachedAlert = fragment.getFragmentManager().
                findFragmentByTag(VerifyRecoverywordsFragment.TAG_TOO_MANY_WRONG_CHOICES_ERROR);
        assertNotNull(choiceLimitReachedAlert);
    }

    @Test
    public void acceptingTooManyAttemptErrorShowsUserRecoveryWords() {
        DialogInterface dialog = mock(DialogInterface.class);
        when(presenter.onSelection("word10")).thenThrow(new VerifyRecoveryWordsPresenter.ChoiceSelectionLimitExceeded());

        fragment.getView().findViewById(R.id.challenge_5).performClick();

        fragment.onClick(dialog, DialogInterface.BUTTON_POSITIVE);

        verify(presenter, times(1)).onShowRecoveryWordsSelected();
    }
}