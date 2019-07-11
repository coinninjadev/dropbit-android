package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletService;
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class RecoverWalletActivityTest {

    private TestCoinKeeperApplication application;
    private ActivityController<RecoverWalletActivity> activityController;
    private RecoverWalletActivity activity;
    private ShadowActivity shadowActivity;
    private String[] invalid_words = {"word1", "word2", "word3", "word4", "word5", "word6",
            "word7", "word8", "word9", "word10", "word11", "word12"};

    private String[] valid_words = {"mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse",
            "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse"};
    private CNWalletServicesInterface cnWalletService = mock(CNWalletService.class);

    @After
    public void tearDown() {
        cnWalletService = null;
        application = null;
        activityController = null;
        invalid_words = null;
        valid_words = null;
        activity = null;
        shadowActivity = null;
    }

    private void mockWithWords(String[] words) {
        Intent intent = new Intent(RuntimeEnvironment.application, RecoverWalletActivity.class);
        intent.putExtra(DropbitIntents.EXTRA_RECOVERY_WORDS, words);
        activityController = Robolectric.buildActivity(RecoverWalletActivity.class, intent);
        activityController.newIntent(intent).create();
        activity = activityController.get();
        activityController.start().resume().visible();
        shadowActivity = shadowOf(activity);
        when(activity.cnServiceConnection.isBounded()).thenReturn(true);
    }

    private void mockForBroadcastTest(String[] recoverWords) {
        Intent intent = new Intent(RuntimeEnvironment.application, RecoverWalletActivity.class);
        intent.putExtra(DropbitIntents.EXTRA_RECOVERY_WORDS, recoverWords);
        activityController = Robolectric.buildActivity(RecoverWalletActivity.class, intent);
        activity = activityController.get();
    }

    @Before
    public void setUp() {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        when(application.cnServiceConnection.getCnWalletServicesInterface()).thenReturn(cnWalletService);
        when(application.bitcoinUtil.isValidBIP39Words(eq(valid_words))).thenReturn(true);
        when(application.bitcoinUtil.isValidBIP39Words(eq(invalid_words))).thenReturn(false);
        mockWithWords(null);
    }


    @Test
    public void successful_recovery_clicking_going_to_wallet_goes_to_next() {
        activity.onSaveRecoveryWordsSuccess();

        Button okButton = activity.findViewById(R.id.ok);
        okButton.performClick();

        assertThat(okButton.getText(), equalTo(activity.getText(R.string.recover_wallet_success_button_text)));
        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(VerificationActivity.class.getName()));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void saving_words_shows_success() {
        activity.onSaveRecoveryWordsSuccess();

        assertThat(((TextView) activity.findViewById(R.id.title)).getText(),
                equalTo(activity.getText(R.string.recover_wallet_success_title)));
        assertThat(((TextView) activity.findViewById(R.id.message)).getText(),
                equalTo(activity.getText(R.string.recover_wallet_success_message)));
        assertThat(activity.findViewById(R.id.close).getVisibility(),
                equalTo(View.INVISIBLE));
    }

    @Test
    public void close_starts_over_at_start_screen() {
        activity.findViewById(R.id.close).performClick();

        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(StartActivity.class.getName()));
        assertTrue(shadowActivity.isFinishing());
        assertThat(activity.findViewById(R.id.close).getVisibility(),
                equalTo(View.VISIBLE));
    }

    @Test
    public void starting_over_navigates_back_to_restore_wallet() {
        mockWithWords(invalid_words);

        Button okButton = activity.findViewById(R.id.ok);
        okButton.performClick();

        assertThat(okButton.getText(), equalTo(activity.getText(R.string.recover_wallet_error_button_text)));
        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(RestoreWalletActivity.class.getName()));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void words_not_in_list_is_error() {
        mockWithWords(invalid_words);

        assertThat(((TextView) activity.findViewById(R.id.title)).getText(),
                equalTo(activity.getText(R.string.recover_wallet_error_title)));
    }

    @Test
    public void error_when_no_words_provided() {
        assertThat(((TextView) activity.findViewById(R.id.title)).getText(),
                equalTo(activity.getText(R.string.recover_wallet_error_title)));
        assertThat(((TextView) activity.findViewById(R.id.message)).getText(),
                equalTo(activity.getText(R.string.recover_wallet_error_message)));
        assertThat(activity.findViewById(R.id.close).getVisibility(),
                equalTo(View.VISIBLE));
    }

    @Test
    public void on_create_instantiate_new_LocalBroadCastUtil_test() {
        mockForBroadcastTest(valid_words);
        activity.localBroadCastUtil = null;

        activityController.create();

        assertNotNull(activity.localBroadCastUtil);
    }

    @Test
    public void on_create_instantiate_new_BroadcastReceiver_test() {
        mockForBroadcastTest(valid_words);
        activity.receiver = null;

        activityController.create();

        assertNotNull(activity.receiver);
    }

    @Test
    public void on_resume_reg_for_save_recovery_words_local_broadcast_test() {
        mockForBroadcastTest(valid_words);
        activityController.create();

        activityController.resume();

        ArgumentCaptor<IntentFilter> argumentCaptor = ArgumentCaptor.forClass(IntentFilter.class);
        verify(application.localBroadCastUtil).registerReceiver(eq(activity.receiver), argumentCaptor.capture());
        IntentFilter filter = argumentCaptor.getValue();
        assertThat(filter.getAction(0), equalTo(DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED));
        assertThat(filter.getAction(1), equalTo(DropbitIntents.ACTION_SAVE_RECOVERY_WORDS));
        assertThat(filter.getAction(2), equalTo(DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS));
    }

    @Test
    public void on_pause_unreg_for_local_broadcast_test() {
        mockForBroadcastTest(valid_words);
        activityController.setup();
        activityController.pause().stop();

        verify(application.localBroadCastUtil).unregisterReceiver(activity.receiver);
    }

    @Test
    public void onCreate_binds_to_CNWalletService_test() {
        mockForBroadcastTest(valid_words);

        activityController.create();
        shadowActivity = shadowOf(activity);

        Intent intent = shadowActivity.peekNextStartedService();
        assertThat(intent.getComponent().getClassName(), equalTo(CNWalletService.class.getName()));

    }

    @Test
    public void onResume_startSaveRecoveryWordsService_test() {
        CNWalletServicesInterface cnServices = mock(CNWalletServicesInterface.class);
        when(activity.cnServiceConnection.isBounded()).thenReturn(true);
        when(activity.cnServiceConnection.getCnWalletServicesInterface()).thenReturn(cnServices);
        mockForBroadcastTest(valid_words);
        activityController.create();
        shadowActivity = shadowOf(activity);

        activityController.resume();

        verify(cnServices).saveSeedWords(valid_words);
    }

    @Test
    public void onResume_dont_do_anything_if_not_bonded_test() {
        CNWalletServicesInterface cnServices = mock(CNWalletServicesInterface.class);
        when(activity.cnServiceConnection.isBounded()).thenReturn(false);
        when(activity.cnServiceConnection.getCnWalletServicesInterface()).thenReturn(cnServices);
        mockForBroadcastTest(valid_words);
        activityController.create();
        shadowActivity = shadowOf(activity);

        activityController.resume();

        verify(cnServices, times(0)).saveSeedWords(valid_words);
    }

    @Test
    public void on_local_broadcast_CONNECTION_BOUNDED_startSaveRecoveryWordsService_test() {
        CNWalletServicesInterface cnServices = mock(CNWalletServicesInterface.class);
        when(activity.cnServiceConnection.isBounded()).thenReturn(true);
        when(activity.cnServiceConnection.getCnWalletServicesInterface()).thenReturn(cnServices);
        mockForBroadcastTest(valid_words);
        activityController.setup();
        LocalBroadCastUtil localBroadCastUtil = activity.localBroadCastUtil;

        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED);

        verify(cnServices).saveSeedWords(valid_words);
    }

    @Test
    public void show_success_when_a_local_broadcast_event_happens_of_save_recovery_success_test() {
        mockForBroadcastTest(valid_words);
        activityController.setup();
        Button mockButton = mock(Button.class);
        TextView mockTitle = mock(TextView.class);
        ImageView mockIcon = mock(ImageView.class);
        TextView mockMessage = mock(TextView.class);
        activity.icon = mockIcon;
        activity.nextButton = mockButton;
        activity.title = mockTitle;
        activity.message = mockMessage;
        LocalBroadCastUtil localBroadCastUtil = new LocalBroadCastUtil(activity);
        activity.localBroadCastUtil = localBroadCastUtil;
        activity.registerForLocalBroadcast();


        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_SAVE_RECOVERY_WORDS);


        verify(mockIcon).setImageResource(R.drawable.ic_restore_success);
        verify(mockTitle).setText(activity.getText(R.string.recover_wallet_success_title));
        verify(mockButton).setText(R.string.recover_wallet_success_button_text);
        verify(mockMessage).setTextColor(activity.getResources().getColor(R.color.font_default));
        verify(mockMessage).setText(R.string.recover_wallet_success_message);
        assertThat(activity.findViewById(R.id.close).getVisibility(), equalTo(View.INVISIBLE));
    }

    @Test
    public void show_fail_when_a_local_broadcast_event_happens_of_save_recovery_fail_test() {
        mockForBroadcastTest(valid_words);
        activityController.setup();
        Button mockButton = mock(Button.class);
        TextView mockTitle = mock(TextView.class);
        ImageView mockIcon = mock(ImageView.class);
        TextView mockMessage = mock(TextView.class);
        activity.icon = mockIcon;
        activity.nextButton = mockButton;
        activity.title = mockTitle;
        activity.message = mockMessage;
        LocalBroadCastUtil localBroadCastUtil = new LocalBroadCastUtil(activity);
        activity.localBroadCastUtil = localBroadCastUtil;
        activity.registerForLocalBroadcast();

        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS);

        verify(mockIcon).setImageResource(R.drawable.ic_restore_fail);
        verify(mockTitle).setText(activity.getText(R.string.recover_wallet_error_title));
        verify(mockButton).setText(R.string.recover_wallet_error_button_text);
        verify(mockMessage).setTextColor(activity.getResources().getColor(R.color.color_error));
        verify(mockMessage).setText(R.string.recover_wallet_error_message);
        assertThat(activity.findViewById(R.id.close).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void show_fail_when_recover_words_are_invalid_test() {
        mockForBroadcastTest(null);
        activityController.setup();
        Button mockButton = mock(Button.class);
        TextView mockTitle = mock(TextView.class);
        ImageView mockIcon = mock(ImageView.class);
        TextView mockMessage = mock(TextView.class);
        activity.icon = mockIcon;
        activity.nextButton = mockButton;
        activity.title = mockTitle;
        activity.message = mockMessage;

        activityController.resume();

        verify(mockIcon).setImageResource(R.drawable.ic_restore_fail);
        verify(mockTitle).setText(activity.getText(R.string.recover_wallet_error_title));
        verify(mockButton).setText(R.string.recover_wallet_error_button_text);
        verify(mockMessage).setTextColor(activity.getResources().getColor(R.color.color_error));
        verify(mockMessage).setText(R.string.recover_wallet_error_message);
        assertThat(activity.findViewById(R.id.close).getVisibility(), equalTo(View.VISIBLE));
    }
}