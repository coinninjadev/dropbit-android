package com.coinninja.coinkeeper.presenter.activity;

import com.coinninja.coinkeeper.service.client.model.CNGlobalMessage;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.service.client.model.InviteMetadata;
import com.coinninja.coinkeeper.service.runner.InviteContactRunner;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import junitx.util.PrivateAccessor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InviteContactPresenterTest {


    @Mock
    private InviteContactRunner inviteRunner;
    @Mock
    private InviteContactPresenter.View view;

    @Mock
    private Analytics analytics;

    @InjectMocks
    private InviteContactPresenter inviteContactPresenter;

    @Before
    public void setUp() throws Exception {
        inviteContactPresenter.attachView(view);
    }

    @Test
    public void inviteContact() {
        Contact contact = mock(Contact.class);
        Long satoshisSending = 5985454L;
        Long bitcoinUSDPrice = 5062L;
        InOrder inOrder = inOrder(inviteRunner);

        when(inviteRunner.clone()).thenReturn(inviteRunner);
        inviteContactPresenter.requestInvite(contact, satoshisSending, bitcoinUSDPrice);

        inOrder.verify(inviteRunner).setOnInviteListener(inviteContactPresenter);
        inOrder.verify(inviteRunner).clone();
        inOrder.verify(inviteRunner).setSatoshisSending(satoshisSending);
        inOrder.verify(inviteRunner).setUSAExchangeCurrency(any(USDCurrency.class));
        inOrder.verify(inviteRunner).execute(contact);
    }

    @Test
    public void onBroadcastProgress() {
        int expectedProgress = 80;

        inviteContactPresenter.onInviteProgress(expectedProgress);

        verify(view).showProgress(expectedProgress);
    }

    @Test
    public void onBroadcastSuccessful() {
        DropBitInvitation result = mock(DropBitInvitation.class);

        inviteContactPresenter.onInviteSuccessful(result);

        verify(view).showInviteSuccessful(result);
        verify(analytics).trackEvent(Analytics.EVENT_DROPBIT_INITIATED);
    }

    @Test
    public void reports_that_user_has_sent_dropbit() {
        inviteContactPresenter.onInviteSuccessful(mock(DropBitInvitation.class));

        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_SENT_DROPBIT, true);
    }

    @Test
    public void degrades_when_failure_to_dispatch_sms() throws JSONException {
        DropBitInvitation inviteContact = mock(DropBitInvitation.class);
        InviteMetadata inviteMetadata = mock(InviteMetadata.class);
        when(inviteContact.getMetadata()).thenReturn(inviteMetadata);
        InviteMetadata.MetadataContact metadataContact = mock(InviteMetadata.MetadataContact.class);
        when(inviteMetadata.getReceiver()).thenReturn(metadataContact);
        int countryCode = 1;
        when(metadataContact.getCountry_code()).thenReturn(countryCode);

        inviteContactPresenter.onInviteSuccessfulDegradedSms(inviteContact);

        verify(view).showInviteSuccessfulDegradedSms(inviteContact);
        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_SENT_DROPBIT, true);
        JSONObject jsonObject = new JSONObject("{" + Analytics.JSON_KEY_COUNTRY_CODE + ":" + countryCode + "}");

        ArgumentCaptor<JSONObject> argumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
        verify(analytics).trackEvent(eq(Analytics.EVENT_DROPBIT_INVITATION_SMS_FAILED), argumentCaptor.capture());

        JSONObject properties = argumentCaptor.getValue();
        Assert.assertThat(jsonObject.toString(), equalTo(properties.toString()));
    }

    @Test
    public void degrades_when_failure_to_dispatch_sms_null_receiver() throws JSONException {
        DropBitInvitation inviteContact = mock(DropBitInvitation.class);
        InviteMetadata inviteMetadata = mock(InviteMetadata.class);
        when(inviteContact.getMetadata()).thenReturn(inviteMetadata);

        inviteContactPresenter.onInviteSuccessfulDegradedSms(inviteContact);

        verify(view).showInviteSuccessfulDegradedSms(inviteContact);
        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_SENT_DROPBIT, true);

        ArgumentCaptor<JSONObject> argumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
        verify(analytics).trackEvent(eq(Analytics.EVENT_DROPBIT_INVITATION_SMS_FAILED), argumentCaptor.capture());

        JSONObject properties = argumentCaptor.getValue();
        Assert.assertThat("{}", equalTo(properties.toString()));
    }

    @Test
    public void onBroadcastError() {

        inviteContactPresenter.onInviteError(Intents.ACTION_DROPBIT__ERROR_UNKNOWN, "error");

        verify(view).showInviteFail(Intents.ACTION_DROPBIT__ERROR_UNKNOWN, "error");
        verify(analytics).trackEvent(Analytics.EVENT_DROPBIT_INITIATION_FAILED);
    }

    @Test
    public void attachView() throws NoSuchFieldException {
        InviteContactPresenter.View expectedView = view;

        inviteContactPresenter.attachView(view);
        InviteContactPresenter.View view = (InviteContactPresenter.View) PrivateAccessor.getField(inviteContactPresenter, "view");

        assertThat(view, equalTo(expectedView));
    }
}