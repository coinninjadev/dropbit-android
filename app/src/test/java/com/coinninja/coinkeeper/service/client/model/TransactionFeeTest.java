package com.coinninja.coinkeeper.service.client.model;

import android.os.Parcel;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class TransactionFeeTest {
    private double minFee;

    private double avgFee;

    private double maxFee;

    @Before
    public void setUp() throws Exception {
        minFee = 2222;
        avgFee = 3333;
        maxFee = 4444;
    }

    @Test
    public void parcelableCreate() {
        Parcel parcel = Parcel.obtain();
        parcel.writeDouble(minFee);
        parcel.writeDouble(avgFee);
        parcel.writeDouble(maxFee);
        parcel.setDataPosition(0);
        TransactionFee transactionFee = (TransactionFee) TransactionFee.CREATOR.createFromParcel(parcel);

        assertThat(transactionFee.getSlow(), equalTo(minFee));
        assertThat(transactionFee.getMed(), equalTo(avgFee));
        assertThat(transactionFee.getFast(), equalTo(maxFee));
    }
}