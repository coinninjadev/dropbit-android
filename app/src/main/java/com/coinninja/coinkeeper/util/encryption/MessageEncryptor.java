package com.coinninja.coinkeeper.util.encryption;

import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.messaging.MessageCryptor;

import javax.inject.Inject;

import app.coinninja.cn.libbitcoin.model.DecryptionKeys;
import app.coinninja.cn.libbitcoin.model.EncryptionKeys;

public class MessageEncryptor {

    private HDWalletWrapper hdWallet;
    private AddressHelper addressHelper;
    private MessageCryptor messageCryptor;

    @Inject
    MessageEncryptor(HDWalletWrapper hdWallet, AddressHelper addressHelper, MessageCryptor messageCryptor) {
        this.hdWallet = hdWallet;
        this.addressHelper = addressHelper;
        this.messageCryptor = messageCryptor;
    }


    public String encrypt(String messsage, String publicKey) {
        EncryptionKeys keys = hdWallet.encryptionKeys(publicKey.getBytes());
        return messageCryptor.encryptAsBase64(messsage.getBytes(), keys.getEncryptionKey(), keys.getHmacKey(),
                keys.getAssociatedPublicKey());
    }

    public String decrypt(String address, String encryptedPayload) {
        Address foundAddress = addressHelper.addressForPubKey(address);
        if (foundAddress == null) return "";
        byte[] ephemeralPublicKey = messageCryptor.unpackEphemeralPublicKey(encryptedPayload);
        DecryptionKeys decryptionKeys = hdWallet.decryptionKeys(foundAddress.getDerivationPath(), ephemeralPublicKey);
        byte[] decrypted = messageCryptor.decrypt(encryptedPayload, decryptionKeys.getEncryptionKey(), decryptionKeys.getHmacKey());

        return new String(decrypted);
    }
}
