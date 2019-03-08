package com.coinninja.coinkeeper.cn.wallet;

import com.coinninja.bindings.Libbitcoin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataSignerTest {

    @Mock
    LibBitcoinProvider libBitcoinProvider;

    @Mock
    Libbitcoin libbitcoin;

    @InjectMocks
    DataSigner dataSigner;


    @Before
    public void setUp() {
        when(libBitcoinProvider.provide()).thenReturn(libbitcoin);
    }

    @Test
    public void provides_verification_key_from_libbitcoin() {
        String key = "--verification key--";
        when(libbitcoin.getCoinNinjaVerificationKey()).thenReturn(key);

        assertThat(dataSigner.getCoinNinjaVerificationKey(), equalTo(key));
    }

    @Test
    public void provides_data_to_be_signed_to_libbitcoin_returning_signed_data() {
        String data_to_sign = "data";
        String signed_data = "signed data";
        when(libbitcoin.sign(data_to_sign)).thenReturn(signed_data);

        String result = dataSigner.sign(data_to_sign);

        assertThat(result, equalTo(signed_data));
    }
}