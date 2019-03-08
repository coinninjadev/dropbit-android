package com.coinninja.coinkeeper.view.fragment;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.Keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class KeyboardFragmentTest {

    private KeyboardFragment fragment;
    private KeyboardFragment.OnKeyPressListener listener;

    @Before
    public void setUp() {
        listener = mock(KeyboardFragment.OnKeyPressListener.class);
        FragmentController<KeyboardFragment> fragmentController = Robolectric.buildFragment(KeyboardFragment.class);
        fragment = fragmentController.get();
        fragmentController.create().start().resume().visible();

    }

    @Test
    public void itRendersKeys() {
        assertNotNull(fragment.getView().findViewById(R.id.keys));
    }

    @Test
    public void doesNotRelayPressedEventsWhenNoOneIsListening() {
        fragment.getView().findViewById(R.id.one).performClick();
    }

    @Test
    public void oneSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.one).performClick();

        verify(listener, times(1)).onKeyPress(Keys.ONE);
    }

    @Test
    public void twoSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.two).performClick();

        verify(listener, times(1)).onKeyPress(Keys.TWO);
    }

    @Test
    public void threeSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.three).performClick();

        verify(listener, times(1)).onKeyPress(Keys.THREE);
    }

    @Test
    public void fourSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.four).performClick();

        verify(listener, times(1)).onKeyPress(Keys.FOUR);
    }

    @Test
    public void fiveSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.five).performClick();

        verify(listener, times(1)).onKeyPress(Keys.FIVE);
    }

    @Test
    public void sixSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.six).performClick();

        verify(listener, times(1)).onKeyPress(Keys.SIX);
    }

    @Test
    public void sevenSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.seven).performClick();

        verify(listener, times(1)).onKeyPress(Keys.SEVEN);
    }

    @Test
    public void eightSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.eight).performClick();

        verify(listener, times(1)).onKeyPress(Keys.EIGHT);
    }

    @Test
    public void nineSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.nine).performClick();

        verify(listener, times(1)).onKeyPress(Keys.NINE);
    }

    @Test
    public void zeroSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.zero).performClick();

        verify(listener, times(1)).onKeyPress(Keys.ZERO);
    }

    @Test
    public void dotSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.dot).performClick();

        verify(listener, times(1)).onKeyPress(Keys.DOT);
    }

    @Test
    public void backSignalsKeyPressedEvent() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.back).performClick();

        verify(listener, times(1)).onKeyPress(Keys.BACK);
    }

    @Test
    public void longPressingBackSendsClearAll() {
        fragment.setOnKeyListener(listener);

        fragment.getView().findViewById(R.id.back).performLongClick();

        verify(listener, times(1)).onKeyPress(Keys.CLEAR);
    }

}