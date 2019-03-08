package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.TransactionNotificationMapper;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionNotificationDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.TransactionNotificationV1;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo;
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SharedMemoRetrievalRunnerTest {

    public static final String T2_ID = "2";
    public static final String T1_ID = "1";
    @Mock
    private TransactionHelper transactionHelper;

    @Mock
    private SignedCoinKeeperApiClient client;

    @Mock
    private DaoSessionManager daoSessionManager;

    @Mock
    private TransactionSummary t1;

    private Response response;

    @Mock
    private TransactionNotificationMapper mapper;

    @Mock
    private TransactionSummary t2;

    @Mock
    private MessageEncryptor messageEncryptor;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private TransactionNotificationDao transactionNotificationDao;

    @InjectMocks
    private SharedMemoRetrievalRunner runner;

    @Before
    public void setUp() {
        List<TransactionSummary> transactions = new ArrayList<>();
        transactions.add(t1);
        transactions.add(t2);
        when(transactionHelper.getRequiringNotificationCheck()).thenReturn(transactions);
        when(t1.getTxid()).thenReturn(T1_ID);
        when(t2.getTxid()).thenReturn(T2_ID);
        when(daoSessionManager.getTransactionNotificationDao()).thenReturn(transactionNotificationDao);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
    }

    @Test
    public void handles_no_memo_for_a_successful_transaction_notification_request() {
        Response response = Response.success(new ArrayList<CNSharedMemo>());
        when(client.getTransactionNotification(T1_ID)).thenReturn(response);
        when(client.getTransactionNotification(T2_ID)).thenReturn(response);

        runner.run();

    }

    @Test
    public void noop_when_not_verified() {
        when(walletHelper.hasVerifiedAccount()).thenReturn(false);
        Response response = Response.success(new ArrayList<CNSharedMemo>());
        when(client.getTransactionNotification(T1_ID)).thenReturn(response);
        when(client.getTransactionNotification(T2_ID)).thenReturn(response);

        runner.run();

        verify(t1, times(0)).setSoughtNotification(true);
        verify(t2, times(0)).setSoughtNotification(true);

    }

    @Test
    public void updates_transactions_with_memos() {
        CNSharedMemo cnSharedMemo = new CNSharedMemo();
        Response response = Response.success(Arrays.asList(cnSharedMemo));
        when(client.getTransactionNotification(T1_ID)).thenReturn(response);

        String json = "{}";
        String decrypted = "{   \n" +
                "     \"meta\": {\n" +
                "       \"version\": 1\n" +
                "     },  \n" +
                "     \"txid\": \"....\",\n" +
                "     \"info\": {\n" +
                "       \"memo\": \"Here's your 5 dollars \uD83D\uDCB8\",\n" +
                "       \"amount\": 500,\n" +
                "       \"currency\": \"USD\"\n" +
                "     },  \n" +
                "     \"profile\": {\n" +
                "       \"display_name\": \"\", \n" +
                "       \"country_code\": 1,\n" +
                "       \"phone_number\": \"3305551122\",\n" +
                "       \"dropbit_me\": \"\", \n" +
                "       \"avatar\": \"aW5zZXJ0IGF2YXRhciBoZXJlCg==\"\n" +
                "     }   \n" +
                "   }   ";

        Response response2 = Response.error(404, ResponseBody.create(MediaType.parse("application/json"),
                json));
        when(client.getTransactionNotification(T2_ID)).thenReturn(response2);
        when(messageEncryptor.decrypt(cnSharedMemo.getAddress(), cnSharedMemo.getEncrypted_payload())).thenReturn(decrypted);
        TransactionNotificationV1 v1 = new TransactionNotificationV1();

        TransactionNotification transactionNotification = new TransactionNotification();
        when(mapper.fromV1(any())).thenReturn(transactionNotification);
        long transactionNotificationId = 3L;
        when(transactionNotificationDao.insert(any())).thenReturn(transactionNotificationId);

        runner.run();

        verify(messageEncryptor).decrypt(cnSharedMemo.getAddress(), cnSharedMemo.getEncrypted_payload());
        verify(transactionHelper).getRequiringNotificationCheck();
        verify(client).getTransactionNotification(T1_ID);
        verify(client).getTransactionNotification(T2_ID);

        verify(t1).setTransactionNotificationId(transactionNotificationId);
        verify(t1).setSoughtNotification(true);
        verify(t1).update();

        verify(t2).setSoughtNotification(true);
        verify(t2).update();
    }


    @Test
    public void skips_messages_that_are_not_in_expected_format() {
        CNSharedMemo cnSharedMemo = new CNSharedMemo();
        Response response = Response.success(Arrays.asList(cnSharedMemo));
        when(client.getTransactionNotification(T1_ID)).thenReturn(response);

        String json = "";

        Response response2 = Response.error(404, ResponseBody.create(MediaType.parse("application/json"),
                json));
        when(client.getTransactionNotification(T2_ID)).thenReturn(response2);
        when(messageEncryptor.decrypt(cnSharedMemo.getAddress(), cnSharedMemo.getEncrypted_payload())).thenThrow(new IndexOutOfBoundsException());
        TransactionNotificationV1 v1 = new TransactionNotificationV1();

        TransactionNotification transactionNotification = new TransactionNotification();
        when(mapper.fromV1(any())).thenReturn(transactionNotification);
        long transactionNotificationId = 3L;
        when(transactionNotificationDao.insert(any())).thenReturn(transactionNotificationId);

        runner.run();

        verify(messageEncryptor).decrypt(cnSharedMemo.getAddress(), cnSharedMemo.getEncrypted_payload());
        verify(transactionHelper).getRequiringNotificationCheck();
        verify(client).getTransactionNotification(T1_ID);
        verify(client).getTransactionNotification(T2_ID);

        verify(t1, times(0)).setTransactionNotificationId(anyLong());
        verify(t1).setSoughtNotification(true);
        verify(t1).update();

        verify(t2, times(0)).setTransactionNotificationId(anyLong());
        verify(t2).setSoughtNotification(true);
        verify(t2).update();
    }

}