package com.coinninja.coinkeeper.presenter.activity;

import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.service.runner.InviteContactRunner;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import junitx.util.PrivateAccessor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InviteContactPresenterTest {

    @Mock
    PendingInviteDTO pendingInviteDTO;

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
        Identity identity = mock(Identity.class);
        when(pendingInviteDTO.getIdentity()).thenReturn(identity);
        InOrder inOrder = inOrder(inviteRunner);

        when(inviteRunner.clone()).thenReturn(inviteRunner);
        inviteContactPresenter.requestInvite(pendingInviteDTO);

        inOrder.verify(inviteRunner).setOnInviteListener(inviteContactPresenter);
        inOrder.verify(inviteRunner).clone();
        inOrder.verify(inviteRunner).execute(identity);
    }

    @Test
    public void onBroadcastProgress() {
        int expectedProgress = 80;

        inviteContactPresenter.onInviteProgress(expectedProgress);

        verify(view).showProgress(expectedProgress);
    }

    @Test
    public void onBroadcastSuccessful() {
        InvitedContact result = mock(InvitedContact.class);

        inviteContactPresenter.onInviteSuccessful(result);

        verify(view).showInviteSuccessful(result);
    }

    @Test
    public void reports_that_user_has_sent_dropbit() {
        inviteContactPresenter.onInviteSuccessful(mock(InvitedContact.class));

        verify(analytics).setUserProperty(Analytics.Companion.PROPERTY_HAS_SENT_DROPBIT, true);
    }

    @Test
    public void onBroadcastError() {

        inviteContactPresenter.onInviteError(DropbitIntents.ACTION_DROPBIT__ERROR_UNKNOWN, "error");

        verify(view).showInviteFail(DropbitIntents.ACTION_DROPBIT__ERROR_UNKNOWN, "error");
    }

    @Test
    public void attachView() throws NoSuchFieldException {
        InviteContactPresenter.View expectedView = view;

        inviteContactPresenter.attachView(view);
        InviteContactPresenter.View view = (InviteContactPresenter.View) PrivateAccessor.getField(inviteContactPresenter, "view");

        assertThat(view, equalTo(expectedView));
    }
}