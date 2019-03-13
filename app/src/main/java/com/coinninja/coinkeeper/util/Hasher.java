package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.BuildConfig;
import com.google.i18n.phonenumbers.Phonenumber;

import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;
import com.coinninja.coinkeeper.model.PhoneNumber;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.inject.Inject;

import androidx.annotation.NonNull;

public class Hasher {

    private static final byte[] SALT = BuildConfig.PHONE_HASH_SALT.getBytes();
    private static final int ITERATIONS = BuildConfig.PHONE_HASH_ITERATIONS;
    private final PKCS5S2ParametersGenerator generator;

    @Inject
    public Hasher() {
        generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
    }

    public String hash(PhoneNumber phoneNumber) {
        return hash(String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getHashReadyPhoneNumber()));
    }

    public String hash(String phoneNumber) {
        SecretKeyFactory secretKeyFactory = null;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA256");
            KeySpec keySpec = new PBEKeySpec(phoneNumber.toCharArray(), SALT, ITERATIONS, 256);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            return Hex.toHexString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return hashViaPKCS5(phoneNumber);
    }

    @NonNull
    private String hashViaPKCS5(String phoneNumber) {
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(phoneNumber.toCharArray()), SALT, ITERATIONS);
        KeyParameter key = (KeyParameter) generator.generateDerivedParameters(256);
        return Hex.toHexString(key.getKey());
    }

}
