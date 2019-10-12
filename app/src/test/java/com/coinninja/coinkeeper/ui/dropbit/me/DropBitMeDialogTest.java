package com.coinninja.coinkeeper.ui.dropbit.me;

import android.app.Dialog;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.home.HomeActivity;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowDialog;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class DropBitMeDialogTest {
    @Mock
    DropbitMeConfiguration dropbitMeConfiguration;
    private ActivityScenario<HomeActivity> scenario;
    @Mock
    private LazyList transactions;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        application.dropbitMeConfiguration = dropbitMeConfiguration;
        when(application.walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.size()).thenReturn(0);

        when(dropbitMeConfiguration.shouldShowWhenPossible()).thenReturn(true);
        when(dropbitMeConfiguration.isNewlyVerified()).thenReturn(false);

        scenario = ActivityScenario.launch(HomeActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @After
    public void tearDown() {
        scenario.close();
        transactions = null;
    }

    @Test
    public void close_dismisses_dialog() {

        Dialog dialog = ShadowDialog.getLatestDialog();
        ShadowDialog shadowDialog = shadowOf(dialog);
        shadowDialog.clickOn(R.id.dialog_close);

        assertTrue(shadowDialog.hasBeenDismissed());
    }

    @Test
    public void hides_title_when_empty() {
        scenario.onActivity(activity -> {
            DropBitMeDialog dialog = (DropBitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);

            assert dialog != null;
            assertThat(dialog.getView().findViewById(R.id.dialog_title).getVisibility(), equalTo(View.GONE));
        });
    }

    @Test
    public void hides_secondary_button_by_default() {
        scenario.onActivity(activity -> {
            DropBitMeDialog dialog = (DropBitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);

            assert dialog != null;
            assertThat(dialog.getView().findViewById(R.id.dialog_secondary_button).getVisibility(), equalTo(View.GONE));
        });
    }
}