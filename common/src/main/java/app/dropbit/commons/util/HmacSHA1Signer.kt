package app.dropbit.commons.util

import android.util.Base64
import app.dropbit.annotations.Mockable
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


@Mockable
class HmacSHA1Signer {
    companion object {
        private const val algorithm = "HmacSHA1"
    }

    fun sign(data: String, secretKey: String): ByteArray {
        val signingKey = SecretKeySpec(secretKey.toByteArray(), algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(signingKey)
        return mac.doFinal(data.toByteArray())
    }

    fun signForDigest(data: String, secretKey: String): ByteArray {
        val signingKey = SecretKeySpec(secretKey.toByteArray(), algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(signingKey)
        return mac.doFinal(data.toByteArray())
    }

    fun signAndEncode(data: String, secretKey: String): String {
        return Base64.encodeToString(sign(data, secretKey),
                Base64.URL_SAFE or Base64.NO_WRAP)
    }

    fun signAndEncodeDigest(data: String, secretKey: String): String {
        return Base64.encodeToString(signForDigest(data, secretKey),
                Base64.URL_SAFE or Base64.NO_WRAP)
    }
}
