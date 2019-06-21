package com.coinninja.coinkeeper.ui.base;

import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.dropbit.me.verified.DisabledDropbitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.verified.NewlyVerifiedDropbitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.verified.VerifiedDropbitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.verify.VerifyDropBitMeDialog;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DropbitMeDialogFactoryTest {

    @Mock
    DropbitMeConfiguration dropbitMeConfiguration;

    @InjectMocks
    DropbitMeDialogFactory factory;

    @After
    public void tearDown() {
        factory = null;
        dropbitMeConfiguration = null;
    }

    @Test
    public void creates_unverified_dropbit_me_dialog_when_not_verified() {
        when(dropbitMeConfiguration.hasVerifiedAccount()).thenReturn(false);

        assertThat(factory.newInstance(), instanceOf(VerifyDropBitMeDialog.class));
    }

    @Test
    public void creates_verified_dropbit_me_dialog_when_verified() {
        when(dropbitMeConfiguration.hasVerifiedAccount()).thenReturn(true);

        assertThat(factory.newInstance(), instanceOf(VerifiedDropbitMeDialog.class));
    }

    @Test
    public void creates_newly_verified_dropbit_me_dialog_when_verified() {
        when(dropbitMeConfiguration.hasVerifiedAccount()).thenReturn(true);
        when(dropbitMeConfiguration.isNewlyVerified()).thenReturn(true);

        assertThat(factory.newInstance(), instanceOf(NewlyVerifiedDropbitMeDialog.class));
    }

    @Test
    public void creates_disabled_verified_dropbit_me_dialog_when_dropbit_me_disabled() {
        when(dropbitMeConfiguration.hasVerifiedAccount()).thenReturn(true);
        when(dropbitMeConfiguration.isDisabled()).thenReturn(true);

        assertThat(factory.newInstance(), instanceOf(DisabledDropbitMeDialog.class));
    }

}