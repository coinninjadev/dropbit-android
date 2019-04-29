package com.coinninja.coinkeeper.view.fragment;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.android.helpers.Views;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsModel;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsPresenter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class VerifyRecoverywordsActivityFragmentTest {


    private final List<String> randomChoices = Arrays.asList("word12", "word6", "word8", "word1", "word10");

    @Mock
    private VerifyRecoveryWordsPresenter presenter;
    private FragmentScenario<VerifyRecoverywordsFragment> scenario;
    private VerifyRecoverywordsFragment fragment;
    private Resources resources;
    private VerifyRecoveryWordsModel recoveryWordsModel = new VerifyRecoveryWordsModel(2, randomChoices);


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        resources = application.getResources();
        when(presenter.startNewChallenge()).thenReturn(recoveryWordsModel);

        FragmentFactory factory = new FragmentFactory() {
            @NonNull
            @Override
            public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
                return VerifyRecoverywordsFragment.newInstance(presenter);
            }
        };

        scenario = FragmentScenario.launchInContainer(VerifyRecoverywordsFragment.class, new Bundle(), R.style.CoinKeeperTheme_Dark_Toolbar, factory);
        scenario.onFragment(frag -> {
            fragment = frag;
        });
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @After
    public void tearDown() {
        presenter = null;
        fragment = null;
        scenario.moveToState(Lifecycle.State.DESTROYED);
        scenario = null;
        recoveryWordsModel = null;
    }

    @Test
    public void itPopulatesWhichWordToLocate() {
        onView(withId(R.id.which_word)).check(matches(withText("2")));
    }

    @Test
    public void itPopulatesFirstRandomWord() {
        assertNotNull(fragment.getView());
        TextView view = Views.withId(fragment.getView(), R.id.challenge_1);

        clickOn(view);

        assertThat(view, hasText("WORD12"));
        verify(presenter).onSelection("word12");
    }

    @Test
    public void itPopulatesSecondRandomWord() {
        assertNotNull(fragment.getView());
        TextView view = Views.withId(fragment.getView(), R.id.challenge_2);

        clickOn(view);

        assertThat(view, hasText("WORD6"));
        verify(presenter).onSelection("word6");
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