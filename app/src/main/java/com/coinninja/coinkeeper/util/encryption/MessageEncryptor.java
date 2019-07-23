package com.coinninja.coinkeeper.util.encryption;

import com.coinninja.bindings.DecryptionKeys;
import com.coinninja.bindings.EncryptionKeys;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.messaging.MessageCryptor;

import javax.inject.Inject;

public class MessageEncryptor {

    @Inject
    HDWallet hdWallet;

    @Inject
    AddressHelper addressHelper;

    @Inject
    MessageCryptor messageCryptor;

    public MessageEncryptor() {

    }

    @Inject
    public MessageEncryptor(HDWallet hdWallet, AddressHelper addressHelper, MessageCryptor messageCryptor) {
        this.hdWallet = hdWallet;
        this.addressHelper = addressHelper;
        this.messageCryptor = messageCryptor;
    }


    public String encrypt(String messsage, String publicKey) {
        EncryptionKeys keys = hdWallet.generateEncryptionKeys(publicKey);
        return messageCryptor.encryptAsBase64(messsage.getBytes(), keys.encryptionKey, keys.hmacKey,
                keys.ephemeralPublicKey);
    }

    public String decrypt(String address, String encryptedPayload) {
        Address foundAddress = addressHelper.addressForPubKey(address);
        byte[] ephemeralPublicKey = messageCryptor.unpackEphemeralPublicKey(encryptedPayload);
        DecryptionKeys decryptionKeys = hdWallet.generateDecryptionKeys(foundAddress.getDerivationPath(), ephemeralPublicKey);
        byte[] decrypted = messageCryptor.decrypt(encryptedPayload, decryptionKeys.encryptionKey, decryptionKeys.hmacKey);

        return new String(decrypted);
    }
}
