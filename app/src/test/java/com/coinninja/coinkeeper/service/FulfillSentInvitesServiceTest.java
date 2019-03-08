package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.service.runner.FulfillSentInvitesRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class FulfillSentInvitesServiceTest {

    private FulfillSentInvitesService service;

    @Before
    public void setUp() {
        service = Robolectric.setupService(FulfillSentInvitesService.class);
        service.fulfillSentInvitesRunner = mock(FulfillSentInvitesRunner.class);
    }

    @Test
    public void successful_full_flow() {
        service.onHandleIntent(null);

        verify(service.fulfillSentInvitesRunner).run();
    }

    @Test
    public void null_out_resources_when_destroyed() {
        service.onDestroy();

        assertNull(service.fulfillSentInvitesRunner);
    }
}