package com.coinninja.coinkeeper.presenter.fragment;

import android.content.Context;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.Shuffler;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import junitx.util.PrivateAccessor;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VerifyRecoveryWordsPresenterTest {

    @Mock
    Context context;

    @Mock
    Shuffler shuffler;

    @Mock
    CNWalletManager cnWalletManager;

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    @Mock
    VerifyRecoveryWordsPresenter.View activity;

    private static final String[] recoveryWords = {
            "word1",
            "word2",
            "word3",
            "word4",
            "word5",
            "word6",
            "word7",
            "word8",
            "word9",
            "word10",
            "word11",
            "word12"
    };

    @InjectMocks
    VerifyRecoveryWordsPresenter presenter;

    @Before
    public void Setup() {
        when(activity.getContext()).thenReturn(context);
        presenter.attach(activity, recoveryWords, 2);
    }


    @Test
    public void pickRandomizesChoices() {
        presenter.startNewChallenge();

        verify(shuffler, times(1)).shuffle(anyList());
    }

    @Test
    public void pickSetsListOfFiveOnModel() {
        VerifyRecoveryWordsModel model = presenter.startNewChallenge();

        assertThat(model.getChoices().size(), equalTo(5));
    }

    private void prepairSelection(int index, String word) throws NoSuchFieldException {
        List<String> mockRecoveryWords = mock(List.class);
        PrivateAccessor.setField(presenter, "recoveryWords", mockRecoveryWords);
        int shuffledIndex = index;
        when(shuffler.pick(5)).thenReturn(shuffledIndex);
        when(mockRecoveryWords.get(shuffledIndex)).thenReturn(word);

    }

    @Test
    public void randomlySelectsWhichIndexToVerify() throws NoSuchFieldException {
        prepairSelection(11, "word12");

        VerifyRecoveryWordsModel model = presenter.pick();

        assertThat(model.getWordNumber(), equalTo(12));
    }

    @Test
    public void verifiesExptectedSelection_whenChoiceIsCorrect() throws NoSuchFieldException {
        prepairSelection(0, "word1");
        presenter.pick();

        assertTrue(presenter.onSelection("word1"));
    }

    @Test
    public void verifiesExptectedSelection_whenChoiceIsNotCorrect() throws NoSuchFieldException {
        prepairSelection(10, "word11");
        presenter.pick();

        assertFalse(presenter.onSelection("word1"));
    }

    @Test(expected = VerifyRecoveryWordsPresenter.ChoiceSelectionLimitExceeded.class)
    public void presentorThrowsAttemptsExceededExceptionWhenSelectionLimitExhausted() throws NoSuchFieldException {
        prepairSelection(10, "word11");
        presenter.pick();

        assertFalse(presenter.onSelection("word1"));
        assertFalse(presenter.onSelection("word1"));
        assertFalse(presenter.onSelection("word1"));
    }

    @Test
    public void itCanBeginANewChallenge() throws NoSuchFieldException {
        prepairSelection(11, "word12");

        VerifyRecoveryWordsModel model = presenter.startNewChallenge();

        assertThat(model.getWordNumber(), equalTo(12));
    }

    @Test
    public void startingNewChallegeResetsAttemptCounter() throws NoSuchFieldException {
        prepairSelection(10, "word11");
        presenter.pick();
        presenter.onSelection("word1");

        presenter.startNewChallenge();
        presenter.onSelection("word1");
        presenter.onSelection("word1");

        assertThat(presenter.getAttemptCounter(), equalTo(2));
    }

    @Test
    public void itCanExcludeAChoices() throws NoSuchFieldException {
        List<String> recoveryWords = mock(List.class);
        PrivateAccessor.setField(presenter, "recoveryWords", recoveryWords);

        presenter.excludeChoice("word2");
        presenter.excludeChoice("word1");
        presenter.pick();

        verify(recoveryWords, times(1)).remove("word1");
        verify(recoveryWords, times(1)).remove("word2");
    }

    @Test
    public void excludesCurrentChallengeFromFuturesWhenSHowingNext() throws NoSuchFieldException {
        presenter.onShowNextChallenge("word11");

        assertThat(presenter.getExcludedChoices().get(0), equalTo("word11"));
    }

    @Test
    public void itWillTellTheViewToShowRecoveryWords() {
        presenter.onShowRecoveryWordsSelected();
        verify(activity, times(1)).showRecoveryWords();
    }

    @Test
    public void itTellsViewThatChallengesAreCompleted() {
        presenter.onShowNextChallenge("word2");
        presenter.onShowNextChallenge("word3");

        verify(activity, times(1)).onChallengeCompleted();
    }

    @Test
    public void itTellsModelToSaveWhenChallengeHasBeenCompleted() {

        presenter.completeChallege();

        verify(cnWalletManager, times(1)).
                userVerifiedWords(recoveryWords);
    }
}