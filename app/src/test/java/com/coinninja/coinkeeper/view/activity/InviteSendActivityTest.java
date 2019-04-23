package com.coinninja.coinkeeper.view.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.presenter.activity.InviteContactPresenter;
import com.coinninja.coinkeeper.service.SaveInviteService;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.progress.SendingProgressView;
import com.coinninja.matchers.ActivityMatchers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class InviteSendActivityTest {
    @Mock
    private InviteContactPresenter invitePresenter;
    @Mock
    private SendingProgressView sendingProgressView;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;

    private long satoshisSpending = 23147L;
    private long satoshisFee = 10108L;
    private Long bitcoinUSDPrice = 800000L;
    private Contact contact;

    private ActivityController<InviteSendActivity> activityController;
    private InviteSendActivity activity;
    private PendingInviteDTO pendingInviteDTO;
    private final PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        contact = new Contact(new PhoneNumber("+13305551111"), "Joe Blow", false);
        pendingInviteDTO = new PendingInviteDTO(contact,
                bitcoinUSDPrice,
                satoshisSpending,
                satoshisFee,
                "--memo--",
                true
        );
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_INVITE_DTO, pendingInviteDTO);

        activityController = Robolectric.buildActivity(InviteSendActivity.class, intent);
        activityController.create();
        activity = activityController.get();
        activity.invitePresenter = invitePresenter;
        activity.sendingProgressView = sendingProgressView;
        activity.activityNavigationUtil = activityNavigationUtil;
    }

    @Test
    public void inviteTransaction() {
        ArgumentCaptor<PendingInviteDTO> argumentCaptor = ArgumentCaptor.forClass(PendingInviteDTO.class);

        activityController.start().resume();

        verify(invitePresenter).requestInvite(argumentCaptor.capture());

        PendingInviteDTO inviteDTO = argumentCaptor.getValue();
        assertThat(inviteDTO.getContact().getPhoneNumber().toNationalDisplayText(), equalTo(this.contact.getPhoneNumber().toNationalDisplayText()));
        assertThat(inviteDTO.getContact().getDisplayName(), equalTo(this.contact.getDisplayName()));
        assertThat(inviteDTO.getContact().isVerified(), equalTo(this.contact.isVerified()));
    }

    @Test
    public void invitePresenter_attach() {
        activityController.start().resume();

        verify(invitePresenter).attachView(activity);
    }

    @Test
    public void showInitTransaction() {
        activityController.start().resume();

        activity.showInitTransaction();

        verify(sendingProgressView, times(2)).setProgress(0);
        verify(sendingProgressView, times(2)).resetView();
    }

    @Test
    public void showProgress() {

        activity.showProgress(22);

        verify(sendingProgressView).setProgress(22);
    }

    @Test
    public void clicking_action_confirming_transaction_navigates_home() {
        activityController.start().resume();
        activity.showInviteSuccessful(mock(InvitedContact.class));

        withId(activity, R.id.transaction_complete_action_button).performClick();

        verify(activityNavigationUtil).navigateToHome(any());
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
        activityController.start().resume();
        activity.showInviteSuccessful(invitedContact);

        verify(sendingProgressView).setProgress(100);
        verify(sendingProgressView).completeSuccess();
        verify(activity.analytics).trackEvent(Analytics.EVENT_DROPBIT_INITIATED);

        Intent intent = new Intent(activity, SaveInviteService.class);
        intent.putExtra(Intents.EXTRA_COMPLETED_INVITE_DTO, new CompletedInviteDTO(pendingInviteDTO, invitedContact));
        assertThat(activity, ActivityMatchers.serviceWithIntentStarted(intent));
    }

    @Test
    public void on_rate_limit_error_test() {
        activityController.start().resume();

        activity.showInviteFail(Intents.ACTION_DROPBIT__ERROR_RATE_LIMIT, "error message");

        DialogFragment dialog = (DialogFragment) activity.getFragmentManager().findFragmentByTag(InviteSendActivity.RATE_LIMIT_DROPBIT_FRAGMENT_TAG);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        Assert.assertThat(messageDisplay.getText().toString(), equalTo("For security reasons we must limit the number of DropBits sent within 30 seconds. Please wait 30 seconds and try sending again."));
        verify(activity.analytics).trackEvent(Analytics.EVENT_DROPBIT_INITIATION_FAILED);
    }
}