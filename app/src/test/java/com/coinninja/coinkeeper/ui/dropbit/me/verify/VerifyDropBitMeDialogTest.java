package com.coinninja.coinkeeper.ui.dropbit.me.verify;

import android.app.Dialog;
import android.content.res.Resources;
import android.widget.Button;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.home.HomeActivity;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowDialog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class VerifyDropBitMeDialogTest {
    private ActivityScenario<HomeActivity> scenario;

    @Mock
    private LazyList transactions;

    @Mock
    private ActivityNavigationUtil activityNavigationUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        application.activityNavigationUtil = activityNavigationUtil;
        when(application.walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.size()).thenReturn(0);

        scenario = ActivityScenario.launch(HomeActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @After
    public void tearDown() {
        scenario.close();
        transactions = null;
    }

    @Test
    public void renders_content() {
        onView(withId(R.id.dropbit_me_button)).perform(click());
        Dialog dialog = ShadowDialog.getLatestDialog();
        Resources resources = dialog.getContext().getResources();

        assertThat(dialog.findViewById(R.id.paragraph_1),
                hasText(resources.getString(R.string.dropbit_me_unverified_paragraph_1)));
        assertThat(dialog.findViewById(R.id.paragraph_2),
                hasText(resources.getString(R.string.dropbit_me_unverified_paragraph_2)));
    }

    @Test
    public void configures_primary_action() {
        onView(withId(R.id.dropbit_me_button)).perform(click());
        Dialog dialog = ShadowDialog.getLatestDialog();
        Resources resources = dialog.getContext().getResources();

        Button button = dialog.findViewById(R.id.dialog_primary_button);
        button.performClick();

        assertThat(button.getText().toString(),
                equalTo(resources.getString(R.string.dropbit_me_verify_my_account_button)));
        verify(activityNavigationUtil).navigateToUserVerification(any());
    }
}