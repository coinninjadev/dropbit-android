package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.presenter.activity.InviteContactPresenter;
import com.coinninja.coinkeeper.service.SaveInviteService;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;
import com.coinninja.coinkeeper.view.progress.SendingProgressView;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(AndroidJUnit4.class)
public class InviteSendActivityTest {
    @Mock
    private InviteContactPresenter inviteContactPresenter;
    @Mock
    private SendingProgressView sendingProgressView;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;

    private long satoshisSpending = 23147L;
    private Long bitcoinUSDPrice = 800000L;
    private Contact contact;
    private ActivityScenario<InviteSendActivity> scenario;
    private Analytics analytics;
    private PendingInviteDTO pendingInviteDTO;

    @After
    public void tearDown() {
        contact = null;
        analytics = null;
        activityNavigationUtil = null;
        sendingProgressView = null;
        inviteContactPresenter = null;
        scenario.close();
    }

    @Before
    public void setUp() {
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        setupDI(application);
        contact = new Contact(new PhoneNumber("+13305551111"), "Joe Blow", false);
        long satoshisFee = 10108L;
        pendingInviteDTO = new PendingInviteDTO(contact,
                bitcoinUSDPrice,
                satoshisSpending,
                satoshisFee,
                "--memo--",
                true
        );
        Intent intent = new Intent(application, InviteSendActivity.class);
        intent.putExtra(DropbitIntents.EXTRA_INVITE_DTO, pendingInviteDTO);
        scenario = ActivityScenario.launch(intent);
        scenario.moveToState(Lifecycle.State.CREATED);
    }

    @Test
    public void inviteTransaction() {
        ArgumentCaptor<PendingInviteDTO> argumentCaptor = ArgumentCaptor.forClass(PendingInviteDTO.class);

        verify(inviteContactPresenter).requestInvite(argumentCaptor.capture());

        PendingInviteDTO inviteDTO = argumentCaptor.getValue();
        assertThat(inviteDTO.getContact().getPhoneNumber().toNationalDisplayText(), equalTo(contact.getPhoneNumber().toNationalDisplayText()));
        assertThat(inviteDTO.getContact().getDisplayName(), equalTo(contact.getDisplayName()));
        assertThat(inviteDTO.getContact().isVerified(), equalTo(contact.isVerified()));
    }

    @Test
    public void invitePresenter_attach() {
        scenario.onActivity(activity -> {
            verify(inviteContactPresenter).attachView(activity);
        });
    }

    @Test
    public void showInitTransaction() {
        scenario.onActivity(activity -> {
            activity.sendingProgressView = sendingProgressView;
        });
        scenario.moveToState(Lifecycle.State.RESUMED);

        verify(sendingProgressView, times(1)).setProgress(0);
        verify(sendingProgressView, times(1)).resetView();
    }

    @Test
    public void showProgress() {
        scenario.onActivity(activity -> {
            activity.sendingProgressView = sendingProgressView;
            activity.showProgress(22);
        });

        verify(sendingProgressView).setProgress(22);
    }

    @Test
    public void clicking_action_confirming_transaction_navigates_home() {
        scenario.onActivity(activity -> {
            activity.showInviteSuccessful(mock(InvitedContact.class));
            clickOn(withId(activity, R.id.transaction_complete_action_button));
            verify(activityNavigationUtil).navigateToHome(any());
        });
    }

    @Test
    public void transactionSuccessful() {
        InvitedContact invitedContact = new InvitedContact(
                "--cn-id--",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "",
                contact.getHash(),
                "",
                ""
        );
        scenario.onActivity(activity -> {
            activity.sendingProgressView = sendingProgressView;
        });
        scenario.moveToState(Lifecycle.State.RESUMED);


        scenario.onActivity(activity -> {
            activity.showInviteSuccessful(mock(InvitedContact.class));
        });

        verify(sendingProgressView).setProgress(100);
        verify(sendingProgressView).completeSuccess();
        verify(analytics).trackEvent(Analytics.EVENT_DROPBIT_INITIATED);

        CompletedInviteDTO completedInviteDTO = new CompletedInviteDTO(pendingInviteDTO, invitedContact);
        Intents.intending(hasComponent(SaveInviteService.class.getName()));
        Intents.intending(hasExtraWithKey(DropbitIntents.EXTRA_COMPLETED_INVITE_DTO));
        Intents.intending(hasExtra(DropbitIntents.EXTRA_COMPLETED_INVITE_DTO, completedInviteDTO));
    }

    @Test
    public void on_rate_limit_error_test() {
        scenario.moveToState(Lifecycle.State.RESUMED);
        scenario.onActivity(activity -> {
            activity.showInviteFail(DropbitIntents.ACTION_DROPBIT__ERROR_RATE_LIMIT, "error message");
        });


        scenario.onActivity(activity -> {
            GenericAlertDialog dialog = (GenericAlertDialog) activity.getSupportFragmentManager()
                    .findFragmentByTag(InviteSendActivity.RATE_LIMIT_DROPBIT_FRAGMENT_TAG);
            Assert.assertThat(dialog.getMessage(),
                    equalTo(activity.getString(R.string.invite_sent_error_rate_limit)));
        });
        verify(analytics).trackEvent(Analytics.EVENT_DROPBIT_INITIATION_FAILED);
    }

    private void setupDI(TestCoinKeeperApplication application) {
        MockitoAnnotations.initMocks(this);
        application.activityNavigationUtil = activityNavigationUtil;
        analytics = application.analytics;
        application.inviteContactPresenter = inviteContactPresenter;
    }
}