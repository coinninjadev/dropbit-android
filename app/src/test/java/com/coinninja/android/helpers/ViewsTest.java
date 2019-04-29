package com.coinninja.android.helpers;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class ViewsTest {

    @Test
    public void finds_view_with_id___given_view() {
        A activity = Robolectric.setupActivity(A.class);
        View view = activity.findViewById(R.id.pager_transaction_details);
        assertThat(withId(view, R.id.ic_send_state), equalTo(view.findViewById(R.id.ic_send_state)));
    }

    @Test
    public void finds_view_with_id___given_alert_dialog() {
        A activity = Robolectric.setupActivity(A.class);
        GenericAlertDialog alertDialog = GenericAlertDialog.newInstance(null, "foo", null, null, null, true, true);
        alertDialog.show(activity.getSupportFragmentManager(), GenericAlertDialog.class.getSimpleName());

        AlertDialog latestAlertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertThat(withId(latestAlertDialog, android.R.id.message), equalTo(latestAlertDialog.findViewById(android.R.id.message)));
    }

    @Test
    public void finds_view_with_id___given_activity() {
        A activity = Robolectric.setupActivity(A.class);

        assertThat(withId(activity, R.id.pager_transaction_details), equalTo(activity.findViewById(R.id.pager_transaction_details)));
    }

    public static class A extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            setTheme(R.style.CoinKeeperTheme_Dark_Toolbar);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transaction_details);
        }
    }
}