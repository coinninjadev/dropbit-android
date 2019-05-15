package com.coinninja.coinkeeper.view.fragment;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.presenter.fragment.PinFragmentPresenter;
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.view.activity.CreatePinActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class PinConfirmFragmentTest {

    private PinConfirmFragment fragment;
    private PinFragmentPresenter pinFragmentPresenter;
    private ActivityController<CreatePinActivity> pinActivityActivityController;

    @Before
    public void setUp() {
        pinFragmentPresenter = mock(PinFragmentPresenter.class);
        pinActivityActivityController = Robolectric.buildActivity(CreatePinActivity.class);
        CreatePinActivity createPinActivity = pinActivityActivityController.setup().get();
        createPinActivity.showConfirmPin();
        fragment = createPinActivity.confirmFragment;
        fragment.setPresenter(pinFragmentPresenter);

    }

    @Test
    public void onResume() {
        View view = fragment.getView();

        assertThat(((TextView) view.findViewById(R.id.headline)).getText().toString(),
                equalTo("Your PIN will be used to access DropBit and send Bitcoin."));
    }

    @Test
    public void onResume_update_activity_label() {
        ActionBarController actionBarController = mock(ActionBarController.class);
        ((BaseActivity) fragment.getActivity()).actionBarController = actionBarController;

        fragment.onResume();

        verify(actionBarController).updateTitle("Re-Enter Pin");
    }

    @Test
    public void onSixDigits() throws Exception {
        int[] pin = {5, 1, 6, 7, 2, 3};

        ((EditText) fragment.getView().findViewById(R.id.pin_entry_edittext)).setText("516723");

        verify(pinFragmentPresenter).pinEntered_Confirm(pin);
    }

    @Test
    public void onDestroy() {
        pinActivityActivityController.destroy();

        verify(pinFragmentPresenter).onDestroyPinConfirm();
    }

    @Test
    public void showPinMismatch() {
        fragment.showPinMismatch();

        assertThat(fragment.getView().findViewById(R.id.error_message).getVisibility(),
                equalTo(View.VISIBLE));
    }

}