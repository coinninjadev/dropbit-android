package com.coinninja.coinkeeper.ui.dropbit;

import android.app.AlertDialog;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.service.client.model.InviteMetadata;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(RobolectricTestRunner.class)
public class ManualInvitePresenterTest {

    private TestableActivity activity;

    private ManualInvitePresenter presenter;

    @Mock
    private ActivityNavigationUtil activityNavigationUtil;
    @Mock
    private DropBitInvitation inviteContact;
    private InviteMetadata metadata;
    private InviteMetadata.MetadataContact receiver;
    private String formattedNumber = "+1 330-555-1111";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.setupActivity(TestableActivity.class);
        receiver = new InviteMetadata.MetadataContact(1, "3305551111");
        metadata = new InviteMetadata();
        metadata.setReceiver(receiver);
        when(inviteContact.getMetadata()).thenReturn(metadata);
        presenter = new ManualInvitePresenter(activityNavigationUtil);
    }

    @After
    public void tearDown() {
        activity = null;
        inviteContact = null;
        presenter = null;
        metadata = null;
        receiver = null;
        formattedNumber = null;
    }

    @Test
    public void shows_when_asked_to_present() {
        presenter.presentManualSend(activity, inviteContact);

        assertNotNull(ShadowAlertDialog.getLatestAlertDialog());
    }

    @Test
    public void shows_receivers_phone_number_in_message() {
        presenter.presentManualSend(activity, inviteContact);

        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        TextView messageView = withId(latestAlertDialog, R.id.message);
        String message = messageView.getText().toString();

        assertTrue(message.contains(formattedNumber));
    }

    @Test
    public void shares_dropbit() {
        presenter.presentManualSend(activity, inviteContact);
        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();

        clickOn(latestAlertDialog, R.id.ok);

        verify(activityNavigationUtil).shareDropbitManually(activity, inviteContact);
    }
}