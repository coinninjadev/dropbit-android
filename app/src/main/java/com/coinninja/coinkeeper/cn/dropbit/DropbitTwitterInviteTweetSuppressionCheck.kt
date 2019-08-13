package com.coinninja.coinkeeper.cn.dropbit

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.SentInvite
import kotlinx.coroutines.*
import retrofit2.Response


@Mockable
class DropbitTwitterInviteTweetSuppressionCheck constructor(
        internal val signedCoinKeeperApiClient: SignedCoinKeeperApiClient
) {
    fun shouldManuallySendTwitterMention(inviteId: String, shouldManuallyMentionCallback: ShouldManuallyMentionCallback) {
        GlobalScope.launch(Dispatchers.Main) {
            checkShouldManuallyMention(inviteId, shouldManuallyMentionCallback)
        }

    }

    @Suppress("UNCHECKED_CAST")
    internal suspend fun checkShouldManuallyMention(inviteId: String, shouldManuallyMentionCallback: ShouldManuallyMentionCallback) {
        coroutineScope {
            val response: Response<SentInvite> = async(Dispatchers.IO) {
                signedCoinKeeperApiClient.patchSuppressionForWalletAddressRequest(inviteId) as Response<SentInvite>
            }.await()

            shouldManuallyMentionCallback.onManualShouldManuallyMention(shouldMentionManually(response))
        }
    }

    private fun shouldMentionManually(response: Response<SentInvite>): Boolean {
        return if (response.isSuccessful) {
            val sentInvite: SentInvite? = response.body()
            !(sentInvite?.isDuplicate ?: false)
        } else {
            true
        }
    }
}