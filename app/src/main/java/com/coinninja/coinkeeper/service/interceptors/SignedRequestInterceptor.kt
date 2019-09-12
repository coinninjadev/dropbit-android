package com.coinninja.coinkeeper.service.interceptors

import android.annotation.SuppressLint
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.util.DateUtil
import com.coinninja.coinkeeper.util.uuid.UuidFactory
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import javax.inject.Inject

@Mockable
class SignedRequestInterceptor @Inject internal constructor(
        internal val dateUtil: DateUtil,
        internal val hdWallet: HDWalletWrapper,
        internal val uuidFactory: UuidFactory,
        internal val cnWalletManager: CNWalletManager) : Interceptor {

    var isThunderDome = false

    @SuppressLint("NewApi")
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(signRequest(chain.request()))
    }

    internal fun signRequest(origRequest: Request): Request {
        val currentTimeFormatted = dateUtil.getCurrentTimeFormatted()
        val builder = origRequest.newBuilder()

        builder.header(CN_AUTH_TIMESTAMP, currentTimeFormatted)
        builder.header(CN_AUTH_DEVICE_UUID, uuidFactory.provideUuid())
        builder.header(CN_AUTH_PUBKEY, hdWallet.verificationKey)

        val body = String(bodyToString(origRequest.body()))
        if (body.isEmpty() || isThunderDome) {
            builder.header(CN_AUTH_SIG, hdWallet.sign(currentTimeFormatted))
        } else {
            builder.header(CN_AUTH_SIG, hdWallet.sign(body))
        }

        addAccountToHeaders(builder)

        return builder.build()
    }

    private fun addAccountToHeaders(builder: Request.Builder) {
        val account = cnWalletManager.account
        if (!account.cnWalletId.isNullOrEmpty()) {
            builder.header(CN_AUTH_WALLET_ID, account.cnWalletId)
        }

        if (!account.cnUserId.isNullOrEmpty()) {
            builder.header(CN_AUTH_USER_ID, account.cnUserId)
        }
    }

    companion object {
        internal const val CN_AUTH_SIG = "cn-auth-signature"
        internal const val CN_AUTH_TIMESTAMP = "cn-auth-timestamp"
        internal const val CN_AUTH_WALLET_ID = "cn-auth-wallet-id"
        internal const val CN_AUTH_USER_ID = "cn-auth-user-id"
        internal const val CN_AUTH_DEVICE_UUID = "cN-auth-device-uuid"
        internal const val CN_AUTH_PUBKEY = "cn-auth-pubkeystring"


        fun bodyToString(request: RequestBody?): ByteArray {
            try {
                val buffer = Buffer()
                if (request != null) {
                    request.writeTo(buffer)
                } else {
                    return "".toByteArray()
                }

                val bytes = ByteArray(buffer.size().toInt())
                buffer.read(bytes)
                return bytes
            } catch (e: IOException) {
                return "did not work".toByteArray()
            }

        }
    }
}
