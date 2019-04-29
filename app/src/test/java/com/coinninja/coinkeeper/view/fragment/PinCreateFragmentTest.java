package com.coinninja.coinkeeper.view.fragment;


import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.presenter.fragment.PinFragmentPresenter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;


@RunWith(AndroidJUnit4.class)
public class PinCreateFragmentTest {
    private PinCreateFragment pinCreateFragment;
    @Mock
    private PinFragmentPresenter pinFragmentPresenter;
    private FragmentScenario<PinCreateFragment> scenario;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        scenario = FragmentScenario.launchInContainer(PinCreateFragment.class);
        scenario.onFragment(new FragmentScenario.FragmentAction<PinCreateFragment>() {
            @Override
            public void perform(@NonNull PinCreateFragment fragment) {
                fragment.setPresenter(pinFragmentPresenter);
                pinCreateFragment = fragment;
            }
        });
    }

    @After
    public void tearDown() {
        pinCreateFragment = null;
        pinFragmentPresenter = null;
        scenario.moveToState(Lifecycle.State.DESTROYED);
    }

    @Test
    public void onResume() {
        View view = pinCreateFragment.getView();

        assertThat(((TextView) view.findViewById(R.id.headline)).getText().toString(),
                equalTo("Your PIN will be used to access DropBit and send Bitcoin."));
    }

    @Test
    public void clears_pin_when_resumed() {
        EditText pinEntry = pinCreateFragment.getView().findViewById(R.id.pin_entry_edittext);
        pinEntry.setText("01234");

        scenario.moveToState(Lifecycle.State.DESTROYED);

        assertThat(pinEntry.getText().toString(), equalTo(""));
    }

    @Test
    public void onSixDigits() {
        int[] userPin = {1, 2, 3, 4, 5, 6};

        EditText pinEntry = pinCreateFragment.getView().findViewById(R.id.pin_entry_edittext);
        pinEntry.setText("123456");

        assertThat(pinEntry.getText().toString(), equalTo(""));
        verify(pinFragmentPresenter).pinEntered_New(userPin);
    }

}