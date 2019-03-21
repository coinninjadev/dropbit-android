package com.coinninja.coinkeeper.view.fragment;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.presenter.fragment.PinFragmentPresenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class PinCreateFragmentTest {
    private PinCreateFragment fragment;
    private FragmentController<PinCreateFragment> fragmentController;
    private PinFragmentPresenter pinFragmentPresenter;

    @Before
    public void setUp() {
        pinFragmentPresenter = mock(PinFragmentPresenter.class);
        fragmentController = Robolectric.buildFragment(PinCreateFragment.class);
        fragment = fragmentController.get();
        fragment.setPresenter(pinFragmentPresenter);
        fragmentController.create().start().resume().visible();
    }


    @Test
    public void onResume() {
        View view = fragment.getView();

        assertThat(((TextView) view.findViewById(R.id.headline)).getText().toString(),
                equalTo("Your PIN will be used to access DropBit and send Bitcoin."));
    }

    @Test
    public void clears_pin_when_resumed() {
        EditText pinEntry = fragment.getView().findViewById(R.id.pin_entry_edittext);
        pinEntry.setText("01234");

        fragmentController.pause().resume();

        assertThat(pinEntry.getText().toString(), equalTo(""));
    }

    @Test
    public void onSixDigits() {
        int[] userPin = {1, 2, 3, 4, 5, 6};

        EditText pinEntry = fragment.getView().findViewById(R.id.pin_entry_edittext);
        pinEntry.setText("123456");

        assertThat(pinEntry.getText().toString(), equalTo(""));
        verify(pinFragmentPresenter).pinEntered_New(userPin);
    }

}