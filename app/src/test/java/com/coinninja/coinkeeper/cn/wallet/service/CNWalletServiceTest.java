package com.coinninja.coinkeeper.cn.wallet.service;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.runner.SaveRecoveryWordsRunner;
import com.coinninja.coinkeeper.di.component.AppComponent;
import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CNWalletServiceTest {
    private String[] valid_words = {"mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse",
            "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse"};

    private CNWalletService service;

    @Mock
    private Handler handler;

    @Mock
    private SaveRecoveryWordsRunner saveRecoveryWordsRunner;

    @Mock
    private FullSyncWalletRunner fullSyncWalletRunner;

    @Mock
    private AppComponent appComponent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = Robolectric.setupService(CNWalletService.class);
        service.workHandler = handler;
        service.appComponent = appComponent;

        when(appComponent.getFullSyncRunner()).thenReturn(fullSyncWalletRunner);
        when(appComponent.getSaveRecoveryWordsRunner()).thenReturn(saveRecoveryWordsRunner);
    }

    @Test
    public void exectues_sync__only_queues_once() {
        ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(Message.class);
        when(handler.hasMessages(25)).thenReturn(false).thenReturn(true);

        service.performSync();
        service.performSync();

        verify(handler).sendMessage(argumentCaptor.capture());

        Message message = argumentCaptor.getValue();
        assertNotNull(message);
        assertThat(message.what, equalTo(25));
        assertThat(message.getCallback(), equalTo(fullSyncWalletRunner));

    }

    @Test
    public void saves_provided_words__only_queues_one_message() {
        ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(Message.class);
        when(handler.hasMessages(35)).thenReturn(false).thenReturn(true);

        service.saveSeedWords(valid_words);
        service.saveSeedWords(valid_words);

        verify(handler).sendMessage(argumentCaptor.capture());

        verify(saveRecoveryWordsRunner).setWords(valid_words);
        Message message = argumentCaptor.getValue();
        assertNotNull(message);
        assertThat(message.what, equalTo(35));
        assertThat(message.getCallback(), equalTo(saveRecoveryWordsRunner));
    }

    @Test
    public void on_bind_return_local_binding_object_test() {
        CNWalletBinder binder = (CNWalletBinder) service.onBind(null);

        assertThat(binder, equalTo(service.cnWalletBinder));
    }

    @Test
    public void on_destroy_stop_background_threads_test() {
        Looper looper = mock(Looper.class);
        when(handler.getLooper()).thenReturn(looper);

        service.onDestroy();

        verify(looper).quitSafely();
    }

}