package com.coinninja.coinkeeper.util.encryption;

import com.coinninja.bindings.DecryptionKeys;
import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.EncryptionKeys;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.messaging.MessageCryptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageEncryptorTest {

    @Mock
    private HDWallet wallet;

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
        when(wallet.generateEncryptionKeys(publicKey)).thenReturn(encryptionKeys);
        when(cryptor.encryptAsBase64(PLAIN_TEXT_MESSAGE.getBytes(),
                ENCRYPTION_KEY, HMAC_KEY, EPHEMERAL_PUBLIC_KEY)).thenReturn(ENCRYPTED_PAYLOAD);

        String actualEncryptedPayload = messageEncryptor.encrypt(PLAIN_TEXT_MESSAGE, publicKey);

        assertThat(actualEncryptedPayload, equalTo(ENCRYPTED_PAYLOAD));
    }

    @Test
    public void testDecrypt() {
        DerivationPath derivationPath = new DerivationPath("m/49/0/0/0/1");
        when(address.getDerivationPath()).thenReturn(derivationPath);
        DecryptionKeys decryptionKeys = new DecryptionKeys(ENCRYPTION_KEY, HMAC_KEY);

        when(addressHelper.get(publicKey)).thenReturn(address);
        when(cryptor.unpackEphemeralPublicKey(ENCRYPTED_PAYLOAD)).thenReturn(EPHEMERAL_PUBLIC_KEY);
        when(wallet.generateDecryptionKeys(derivationPath, EPHEMERAL_PUBLIC_KEY)).thenReturn(decryptionKeys);
        when(cryptor.decrypt(ENCRYPTED_PAYLOAD, ENCRYPTION_KEY, HMAC_KEY)).thenReturn(
                PLAIN_TEXT_MESSAGE.getBytes());

        String decryptedPayload = messageEncryptor.decrypt(publicKey, ENCRYPTED_PAYLOAD);

        assertThat(PLAIN_TEXT_MESSAGE, equalTo(decryptedPayload));
    }

}