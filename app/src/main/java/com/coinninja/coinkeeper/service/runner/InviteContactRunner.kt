package com.coinninja.coinkeeper.service.runner

import android.os.AsyncTask
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.*
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.DropbitIntents
import retrofit2.Response
import javax.inject.Inject

@Mockable
class InviteContactRunner @Inject internal constructor(
        internal val client: SignedCoinKeeperApiClient,
        internal val dropbitAccountHelper: DropbitAccountHelper,
        internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
        internal val cnLogger: CNLogger) : AsyncTask<Identity, Int, Response<*>>() {

    internal var onInviteListener: OnInviteListener? = null
    internal var pendingInviteDTO: PendingInviteDTO? = null


    private constructor(client: SignedCoinKeeperApiClient,
                        dropbitAccountHelper: DropbitAccountHelper,
                        pendingInviteDTO: PendingInviteDTO?,
                        onInviteListener: OnInviteListener?,
                        inviteTransactionSummaryHelper:
                        InviteTransactionSummaryHelper, cnLogger: CNLogger) : this(client, dropbitAccountHelper, inviteTransactionSummaryHelper, cnLogger) {

        this.onInviteListener = onInviteListener
        this.pendingInviteDTO = pendingInviteDTO
    }

    fun setPendingInviteDTO(pendingInviteDTO: PendingInviteDTO) {
        this.pendingInviteDTO = pendingInviteDTO
    }

    fun setOnInviteListener(onInviteListener: OnInviteListener) {
        this.onInviteListener = onInviteListener
    }

    fun clone(): InviteContactRunner {
        return InviteContactRunner(client, dropbitAccountHelper, pendingInviteDTO, onInviteListener, inviteTransactionSummaryHelper, cnLogger)
    }

    override fun doInBackground(vararg identities: Identity): Response<*>? {
        var response: Response<*>? = null
        pendingInviteDTO?.let { pendingDTO ->
            publishProgress(50)

            inviteTransactionSummaryHelper.saveTemporaryInvite(pendingDTO)

            val identity = identities[0]

            val sendingValue = BTCCurrency(pendingDTO.inviteAmount)
            val payload = InviteUserPayload(
                    Amount(sendingValue.toSatoshis(), sendingValue.toUSD(USDCurrency(pendingDTO.bitcoinPrice)).toLong()),
                    getSenderForIdentityInvite(identity),
                    getReceiverFromIdentity(identity),
                    pendingDTO.requestId
            )

            response = client.inviteUser(payload)

            if (response?.isSuccessful != true) {
                cnLogger.logError(TAG, "|---- Invite Contact failed", response)
            }
        }

        return response
    }

    override fun onPostExecute(response: Response<*>?) {
        if (response == null) {
            onInviteListener?.onInviteError(DropbitIntents.ACTION_DROPBIT__ERROR_UNKNOWN, getErrorMessage(response))
            return
        }

        if (response.isSuccessful) {
            val inviteContact = response.body() as InvitedContact
            onInviteListener?.onInviteSuccessful(inviteContact)
        } else {
            onResponseError(response)
        }
    }

    override fun onProgressUpdate(vararg values: Int?) {
        values[0]?.let { onInviteListener?.onInviteProgress(it) }
    }

    private fun getReceiverFromIdentity(identity: Identity): Receiver {
        val type = identity.identityType
        var value = identity.value
        val handle = identity.displayName

        if (identity.identityType == IdentityType.PHONE) {
            val phoneNumber = PhoneNumber(identity.value)
            value = "${phoneNumber.countryCode}${phoneNumber.nationalNumber}"
        }

        return Receiver(type.asString(), value, handle)
    }

    internal fun getSenderForIdentityInvite(identity: Identity): Sender {
        var type = identity.identityType
        var value = ""
        var handle: String? = null

        if (type == IdentityType.PHONE && dropbitAccountHelper.isPhoneVerified) {
            dropbitAccountHelper.phoneIdentity()?.let {
                type = it.type
                val phoneNumber = PhoneNumber(it.identity)
                value = "${phoneNumber.countryCode}${phoneNumber.nationalNumber}"
            }
        } else {
            dropbitAccountHelper.twitterIdentity()?.let {
                type = it.type
                value = it.identity
                handle = it.handle
            }
        }

        return Sender(type.asString(), value, handle)
    }

    private fun onResponseError(errorResponse: Response<*>) {
        when (errorResponse.code()) {
            429 -> onInviteListener?.onInviteError(DropbitIntents.ACTION_DROPBIT__ERROR_RATE_LIMIT, getErrorMessage(errorResponse))
            else -> onInviteListener?.onInviteError(DropbitIntents.ACTION_DROPBIT__ERROR_UNKNOWN, getErrorMessage(errorResponse))
        }
    }

    private fun getErrorMessage(response: Response<*>?): String {
        val unknown = "unknown"
        if (response == null) return unknown

        try {
            return response.errorBody()?.string() ?: unknown
        } catch (e: Exception) {
            e.printStackTrace()
            return unknown
        }

    }

    interface OnInviteListener {
        fun onInviteSuccessful(contact: InvitedContact)

        fun onInviteProgress(progress: Int)

        fun onInviteError(dropBitActionError: String, errorMessage: String)
    }

    companion object {
        private val TAG = InviteContactRunner::class.java.simpleName
    }
}
