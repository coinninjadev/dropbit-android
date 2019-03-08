package com.coinninja.coinkeeper.service;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.runner.SaveTransactionRunner;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BroadcastTransactionServiceTest {

    @Mock
    SaveTransactionRunner saveTransactionRunner;
    private BroadcastTransactionService broadcastTransactionService;
    private CompletedBroadcastDTO completedBroadcastActivityDTO;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        broadcastTransactionService = Robolectric.setupService(BroadcastTransactionService.class);
        broadcastTransactionService.runner = saveTransactionRunner;
        BroadcastTransactionDTO broadcastActivityDTO = new BroadcastTransactionDTO(
                mock(UnspentTransactionHolder.class),
                new Contact(new PhoneNumber("+13333333333"), "Joe Blow", true),
                false, "--memo--",
                null);
        completedBroadcastActivityDTO = new CompletedBroadcastDTO(broadcastActivityDTO, "--txid--");
    }

    @Test
    public void saves_transaction_locally() {
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_COMPLETED_BROADCAST_DTO, completedBroadcastActivityDTO);

        broadcastTransactionService.onHandleIntent(intent);

        InOrder ordered = inOrder(saveTransactionRunner);
        ordered.verify(saveTransactionRunner).setCompletedBroadcastActivityDTO(completedBroadcastActivityDTO);
        ordered.verify(saveTransactionRunner).run();
    }
}

