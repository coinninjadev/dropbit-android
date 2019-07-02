package com.coinninja.coinkeeper.cn.wallet;

import com.coinninja.bindings.DecryptionKeys;
import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.EncryptionKeys;
import com.coinninja.bindings.Libbitcoin;
import com.coinninja.coinkeeper.cn.wallet.data.HDWalletTestData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HDWalletTest {

    @Mock
    LibBitcoinProvider libBitcoinProvider;

    @Mock
    private Libbitcoin libbitcoin;

    @InjectMocks
    private HDWallet wallet;

    @Before
    public void setUp() throws Exception {
        when(libBitcoinProvider.provide()).thenReturn(libbitcoin);
        prepareAddresses();
    }

    @Test
    public void fillsExternalBlocksAtStartingIndex() {
        String[] addresses = wallet.fillBlock(HDWallet.EXTERNAL, 0, 50);

        assertThat(addresses, equalTo(HDWalletTestData.blockOneExternalAddresses));
    }

    @Test
    public void fillsInternalBlocksAtStartingIndex() {
        String[] addresses = wallet.fillBlock(HDWallet.INTERNAL, 0, 50);


        assertThat(addresses, equalTo(HDWalletTestData.blockOneInternalAddresses));
    }

    @Test
    public void returns_external_address_for_index() {
        String address = "--ex-address";
        when(libbitcoin.getExternalChangeAddress(0)).thenReturn(address);

        assertThat(wallet.getExternalAddress(0), equalTo(address));
    }

    @Test
    public void generateEncryptionKeys() {
        String publicKey = "public";
        EncryptionKeys encryptionKeys = new EncryptionKeys("".getBytes(), "".getBytes(), "".getBytes());
        when(libbitcoin.getEncryptionKeys(publicKey)).thenReturn(encryptionKeys);

        EncryptionKeys keys = wallet.generateEncryptionKeys(publicKey);
        assertThat(keys, equalTo(encryptionKeys));
    }

    @Test
    public void generateDecryptionKeys() {
        DecryptionKeys decryptionKeys = new DecryptionKeys("".getBytes(), "".getBytes());

        DerivationPath derivationPath = new DerivationPath("m/49/0/0/0/1");

        byte[] ephemeralPublicKey = "ephemeralPublicKey".getBytes();
        when(libbitcoin.getDecryptionKeys(derivationPath, ephemeralPublicKey)).thenReturn(decryptionKeys);

        DecryptionKeys keys = wallet.generateDecryptionKeys(derivationPath, ephemeralPublicKey);

        assertThat(keys, equalTo(decryptionKeys));
    }

    private void prepareAddresses() {
        //External
        for (int i = 0; i < HDWalletTestData.blockOneExternalAddresses.length; i++) {
            when(libbitcoin.getExternalChangeAddress(i)).thenReturn(HDWalletTestData.blockOneExternalAddresses[i]);
        }

        //Internal
        for (int i = 0; i < HDWalletTestData.blockOneExternalAddresses.length; i++) {
            when(libbitcoin.getInternalChangeAddress(i)).thenReturn(HDWalletTestData.blockOneInternalAddresses[i]);
        }
    }
}