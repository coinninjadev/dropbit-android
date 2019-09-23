package com.coinninja.coinkeeper.util

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.BuildConfig
import com.coinninja.coinkeeper.model.PhoneNumber
import org.spongycastle.crypto.PBEParametersGenerator
import org.spongycastle.crypto.digests.SHA256Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.params.KeyParameter
import org.spongycastle.util.encoders.Hex
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

@Mockable
class Hasher @Inject constructor() {

    private val generator: PKCS5S2ParametersGenerator = PKCS5S2ParametersGenerator(SHA256Digest())

    fun hash(phoneNumber: PhoneNumber): String {
        return hash(String.format("%s%s", phoneNumber.countryCode, phoneNumber.hashReadyPhoneNumber))
    }

    fun hash(value: String): String {
        return String(hash(value.toCharArray()))
    }

    fun hash(value: CharArray): CharArray {
        var secretKeyFactory: SecretKeyFactory? = null
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA256")
            val keySpec: KeySpec = PBEKeySpec(value, SALT, ITERATIONS, 256)
            val secretKey: SecretKey = secretKeyFactory.generateSecret(keySpec)
            return Hex.toHexString(secretKey.encoded).toCharArray()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return hashViaPKCS5(value).toCharArray()
    }

    private fun hashViaPKCS5(value: CharArray): String {
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(value), SALT, ITERATIONS)
        val key = generator.generateDerivedParameters(256) as KeyParameter
        return Hex.toHexString(key.key)
    }

    companion object {
        private val SALT = BuildConfig.PHONE_HASH_SALT.toByteArray()
        private const val ITERATIONS = BuildConfig.PHONE_HASH_ITERATIONS
    }

}