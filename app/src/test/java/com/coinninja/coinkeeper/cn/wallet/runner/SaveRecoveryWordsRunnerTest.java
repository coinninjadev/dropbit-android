package com.coinninja.coinkeeper.cn.wallet.runner;

import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SaveRecoveryWordsRunnerTest {
    @Mock
    CNWalletManager cnWalletManager;
    @Mock
    AccountManager accountManager;


    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    @InjectMocks
    SaveRecoveryWordsRunner runner;

    String[] recoveryWords = {"word", "word", "word", "word", "word", "word", "word",
            "word", "word", "word", "word", "word",};

    @After
    public void tearDown() {
        runner = null;
        cnWalletManager = null;
        localBroadCastUtil = null;
        recoveryWords = null;
    }


    @Test
    public void does_not_save_when_words_null_or_empty() {
        runner.run();

        verify(cnWalletManager, times(0)).userVerifiedWords(any());
    }

    @Test
    public void saves_provided_words() {
        runner.setWords(recoveryWords);

        runner.run();

        verify(cnWalletManager).userVerifiedWords(recoveryWords);
    }

    @Test
    public void dispatches_event_that_words_saved_successfully() {
        runner.setWords(recoveryWords);
        when(cnWalletManager.userVerifiedWords(recoveryWords)).thenReturn(true);

        runner.run();

        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_SAVE_RECOVERY_WORDS);
    }

    @Test
    public void dispatches_event_that_words_not_saved_due_to_cn_validation() {
        runner.setWords(recoveryWords);
        when(cnWalletManager.userVerifiedWords(recoveryWords)).thenReturn(false);

        runner.run();

        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS);
    }

    @Test
    public void dispatches_event_that_words_not_saved_due_to_no_words() {
        runner.run();

        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS);
    }
}