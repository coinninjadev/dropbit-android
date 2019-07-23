package com.coinninja.coinkeeper.service.runner

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.service.client.model.SentInvite
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class SentInvitesStatusGetterTest {
    companion object {
        private var response = "[\n" +
                "  {\n" +
                "    \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921356,\n" +

                "    \"address\": \"\",\n" +
                "    \"metadata\": {\n" +
                "      \"amount\": {\n" +
                "        \"btc\": 120000000,\n" +
                "        \"usd\": 8292280\n" +
                "      },\n" +
                "      \"sender\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      },\n" +
                "      \"receiver\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      },\n" +
                "      \"request_id\": \"3fbdc415-8789-490a-ad32-0c6fa3590182\"" +
                "    },\n" +
                "    \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"status\": \"new\",\n" +
                "    \"txid\": \"\",\n" +
                "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "  }\n" +
                "]"
    }

    private var invite: SentInvite = mock()
    private var newSummary: InviteTransactionSummary = mock()
    private var oldSummary: InviteTransactionSummary = mock()

    private val badResponse: Response<*>
        get() = Response.error<Any>(500,
                ResponseBody.create(
                        MediaType.parse("application/json"), ""))

    private fun createRunner(): SentInvitesStatusGetter {
        return SentInvitesStatusGetter(
                ApplicationProvider.getApplicationContext(),
                mock(), mock(), mock(), mock())
    }

    @Test
    fun update_new_invites() {
        val status = "new"
        val runner = createRunner()
        mockSendForStatus(runner, status, false)

        whenever(runner.inviteTransactionSummaryHelper.getInviteSummaryById(invite.id)).thenReturn(newSummary)
        whenever(runner.inviteTransactionSummaryHelper.getInviteSummaryById(invite.metadata.request_id)).thenReturn(oldSummary)

        runner.run()

        verify(runner.inviteTransactionSummaryHelper).updateInviteAddressTransaction(invite)
        verify(runner.inviteTransactionSummaryHelper).acknowledgeInviteTransactionSummary(invite)
    }

    @Test
    fun does_nothing_when_server_out() {
        val runner = createRunner()
        whenever(runner.client.sentInvites).thenReturn(badResponse)

        runner.run()

        verify(runner.inviteTransactionSummaryHelper, never()).updateInviteAddressTransaction(any())
    }

    @Test
    fun updates_completed_invites() {
        val runner = createRunner()
        val status = "completed"
        mockSendForStatus(runner, status, false)
        whenever(runner.inviteTransactionSummaryHelper.getInviteSummaryById(invite.id)).thenReturn(newSummary)

        runner.run()

        verify(runner.inviteTransactionSummaryHelper).updateInviteAddressTransaction(invite)
    }

    @Test
    fun updates_canceled_invites() {
        val runner = createRunner()
        val status = "canceled"
        mockSendForStatus(runner, status, false)

        runner.run()

        verify(runner.inviteTransactionSummaryHelper).updateInviteAddressTransaction(invite)
    }

    @Test
    fun updates_expired_invites() {
        val runner = createRunner()
        val status = "expired"
        mockSendForStatus(runner, status, false)

        whenever(runner.inviteTransactionSummaryHelper.getInviteSummaryById(invite.id)).thenReturn(newSummary)

        runner.run()

        verify(runner.inviteTransactionSummaryHelper).updateInviteAddressTransaction(invite)
    }

    @Test
    fun sets_internal_notification_for_user_when_invite_expires_for_send() {
        val runner = createRunner()
        val formattedPhone = "(330) 555-1111"
        val status = "expired"
        mockSendForStatus(runner, status, true)
        whenever(newSummary.localeFriendlyDisplayIdentityForReceiver).thenReturn(formattedPhone)

        runner.run()
        verify(runner.internalNotificationHelper).addNotifications(
                runner.context.getString(R.string.invite_send_expired_message, formattedPhone))
    }

    @Test
    fun sets_internal_notification_for_user_when_invite_is_canceled_for_send() {
        val runner = createRunner()
        val status = "canceled"
        val formattedPhone = "(330) 555-1111"
        mockSendForStatus(runner, status, true)
        whenever(newSummary.localeFriendlyDisplayIdentityForReceiver).thenReturn(formattedPhone)

        runner.run()
        val btc = BTCCurrency(1000L)
        btc.currencyFormat = BTCCurrency.ALT_CURRENCY_FORMAT

        verify(runner.internalNotificationHelper).addNotifications(
                runner.context.getString(R.string.invite_send_canceled_message,
                        "(330) 555-1111", btc.toFormattedCurrency()))

    }

    private fun mockSendForStatus(runner: SentInvitesStatusGetter, status: String, simulateChange: Boolean) {
        val response = getResponse(status)
        invite = (response.body() as List<*>)[0] as SentInvite
        newSummary = mock()
        oldSummary = mock()
        whenever(newSummary.btcState).thenReturn(BTCState.from(status))
        whenever(newSummary.type).thenReturn(Type.SENT)
        whenever(newSummary.valueSatoshis).thenReturn(1000L)
        whenever(oldSummary.type).thenReturn(Type.SENT)

        if (simulateChange) {
            whenever(oldSummary.btcState).thenReturn(BTCState.UNFULFILLED)
        } else {
            whenever(oldSummary.btcState).thenReturn(BTCState.from(status))
        }

        whenever(runner.client.sentInvites).thenReturn(response)
        whenever(runner.inviteTransactionSummaryHelper.getInviteSummaryById(invite.id)).thenReturn(oldSummary)
        whenever(runner.inviteTransactionSummaryHelper.updateInviteAddressTransaction(invite)).thenReturn(newSummary)
    }

    private fun getResponse(status: String): Response<*> {
        val invites = Gson().fromJson<List<SentInvite>>(response, object : TypeToken<List<SentInvite>>() {

        }.type)

        for (invite in invites) {
            invite.status = status
        }
        return Response.success(invites)
    }


}