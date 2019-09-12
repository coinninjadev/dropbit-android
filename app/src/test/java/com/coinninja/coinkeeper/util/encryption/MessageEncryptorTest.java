package com.coinninja.coinkeeper.util.encryption;

import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.messaging.MessageCryptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import app.coinninja.cn.libbitcoin.model.DecryptionKeys;
import app.coinninja.cn.libbitcoin.model.DerivationPath;
import app.coinninja.cn.libbitcoin.model.EncryptionKeys;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageEncryptorTest {

    @Mock
    private HDWalletWrapper wallet;

    @Mock
    private MessageCryptor cryptor;

    @Mock
    private AddressHelper addressHelper;

    @Mock
    private Address address;

    @InjectMocks
    MessageEncryptor messageEncryptor;


    private static final byte[] ENCRYPTION_KEY = "1".getBytes();
    private static final byte[] HMAC_KEY = "2".getBytes();
    private static final byte[] EPHEMERAL_PUBLIC_KEY = "3".getBytes();
    private static final String publicKey = "publicKey";
    private static final String ENCRYPTED_PAYLOAD = "KJKLJLKJLKJLK";
    private static final String PLAIN_TEXT_MESSAGE = "messsage";


    @Test
    public void testEncrypt() {
        EncryptionKeys encryptionKeys = new EncryptionKeys(ENCRYPTION_KEY, HMAC_KEY,
                EPHEMERAL_PUBLIC_KEY);
        when(wallet.encryptionKeys(publicKey.getBytes())).thenReturn(encryptionKeys);
        when(cryptor.encryptAsBase64(PLAIN_TEXT_MESSAGE.getBytes(),
                ENCRYPTION_KEY, HMAC_KEY, EPHEMERAL_PUBLIC_KEY)).thenReturn(ENCRYPTED_PAYLOAD);

        String actualEncryptedPayload = messageEncryptor.encrypt(PLAIN_TEXT_MESSAGE, publicKey);

        assertThat(actualEncryptedPayload, equalTo(ENCRYPTED_PAYLOAD));
    }

    @Test
    public void testDecrypt() {
        DerivationPath derivationPath = DerivationPath.CREATOR.from("M/49/0/0/0/1");
        when(address.getDerivationPath()).thenReturn(derivationPath);
        DecryptionKeys decryptionKeys = new DecryptionKeys(ENCRYPTION_KEY, HMAC_KEY);

        when(addressHelper.addressForPubKey(publicKey)).thenReturn(address);
        when(cryptor.unpackEphemeralPublicKey(ENCRYPTED_PAYLOAD)).thenReturn(EPHEMERAL_PUBLIC_KEY);
        when(wallet.decryptionKeys(derivationPath, EPHEMERAL_PUBLIC_KEY)).thenReturn(decryptionKeys);
        when(cryptor.decrypt(ENCRYPTED_PAYLOAD, ENCRYPTION_KEY, HMAC_KEY)).thenReturn(
                PLAIN_TEXT_MESSAGE.getBytes());

        String decryptedPayload = messageEncryptor.decrypt(publicKey, ENCRYPTED_PAYLOAD);

        assertThat(PLAIN_TEXT_MESSAGE, equalTo(decryptedPayload));
    }

}