package com.coinninja.coinkeeper.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coinninja.coinkeeper.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ConfirmationsViewTest {

    private ConfirmationsView confirmationsView;

    @Before
    public void setUp() {
        A a = Robolectric.setupActivity(A.class);
        confirmationsView = a.findViewById(R.id.confirmations);
    }

    @After
    public void tearDown() {
        confirmationsView = null;
    }

    @Test
    public void setting_dropbit_configuration_defaults_to_transaction() {
        confirmationsView.setConfiguration(ConfirmationsView.CONFIGURATION_DROPBIT);
        assertThat(confirmationsView.getNumStages(), equalTo(5));
        assertThat(confirmationsView.calcStageOffset(), equalTo(0));

        confirmationsView.setConfiguration(ConfirmationsView.CONFIGURATION_TRANSACTION);
        assertThat(confirmationsView.getNumStages(), equalTo(3));
        assertThat(confirmationsView.calcStageOffset(), equalTo(-2));

        confirmationsView.setConfiguration(-1);
        assertThat(confirmationsView.getNumStages(), equalTo(3));
    }

    @Test
    public void setting_dropbit_state_defaults_to_start() {
        confirmationsView.setConfiguration(ConfirmationsView.CONFIGURATION_DROPBIT);

        confirmationsView.setStage(ConfirmationsView.STAGE_DROPBIT_SENT);
        assertThat(confirmationsView.getCurrentStep(), equalTo(1));

        confirmationsView.setStage(ConfirmationsView.STAGE_ADDRESS_RECEIVED);
        assertThat(confirmationsView.getCurrentStep(), equalTo(2));

        confirmationsView.setStage(ConfirmationsView.STAGE_BROADCASTING);
        assertThat(confirmationsView.getCurrentStep(), equalTo(3));

        confirmationsView.setStage(ConfirmationsView.STAGE_PENDING);
        assertThat(confirmationsView.getCurrentStep(), equalTo(4));

        confirmationsView.setStage(ConfirmationsView.STAGE_COMPLETE);
        assertThat(confirmationsView.getCurrentStep(), equalTo(5));

        confirmationsView.setStage(-1);
        assertThat(confirmationsView.getCurrentStep(), equalTo(1));
    }

    @Test
    public void calculates_radius_from_diameter() {
        confirmationsView.setDiameter(20F);

        assertThat(confirmationsView.calcBeadRadius(), equalTo(10F));
    }

    public static class A extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            setTheme(R.style.CoinKeeperTheme_Dark_Toolbar);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.confirmations);
        }
    }

}