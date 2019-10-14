package com.coinninja.coinkeeper.cn.transaction.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.thunderdome.model.LedgerInvoice
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.cn.wallet.tx.FundingModel
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.UserIdentity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.*
import com.coinninja.coinkeeper.util.FeesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

@Suppress("UNCHECKED_CAST")
@Mockable
class FundingViewModel : ViewModel() {
    lateinit var transactionNotificationManager: TransactionNotificationManager
    lateinit var inviteTransactionSummaryHelper: InviteTransactionSummaryHelper
    lateinit var transactionFundingManager: TransactionFundingManager
    lateinit var thunderDomeRepository: ThunderDomeRepository
    lateinit var feesManager: FeesManager
    lateinit var fundingModel: FundingModel
    lateinit var cnApiClient: SignedCoinKeeperApiClient

    var invitedContactResponse: MutableLiveData<InvitedContact> = MutableLiveData()
    var transactionData: MutableLiveData<TransactionData> = MutableLiveData()
    var lightningWithdrawalDropbitFee: MutableLiveData<BTCCurrency> = MutableLiveData()
    var lightningWithdrawalNetworkFee: MutableLiveData<BTCCurrency> = MutableLiveData()
    var lightningWithdrawalCompleted: MutableLiveData<Boolean> = MutableLiveData()
    var addressLookupResult: MutableLiveData<AddressLookupResult> = MutableLiveData()
    var pendingLedgerInvoice: MutableLiveData<LedgerInvoice> = MutableLiveData()
    var ledgerInvoice: MutableLiveData<LedgerInvoice> = MutableLiveData()

    fun clear() {
        transactionData = MutableLiveData()
        lightningWithdrawalDropbitFee = MutableLiveData()
        lightningWithdrawalCompleted = MutableLiveData()
        lightningWithdrawalNetworkFee = MutableLiveData()
        addressLookupResult = MutableLiveData()
        pendingLedgerInvoice = MutableLiveData()
        invitedContactResponse = MutableLiveData()
    }

    fun fundLightningDeposit(btcAmount: Long): LiveData<TransactionData> {
        viewModelScope.launch(Dispatchers.Main) {
            transactionData.value = withContext(Dispatchers.IO) {
                thunderDomeRepository.lightningAccount?.let {
                    val transactionData = transactionFundingManager.buildFundedTransactionData(it.address, feesManager.fee(FeesManager.FeeType.CHEAP), btcAmount.toLong(), false)
                    if (transactionData.utxos.isNotEmpty()) {
                        transactionData.paymentAddress = it.address
                    }
                    transactionData
                }
            }
        }
        return transactionData
    }

    fun fundLightningWithdrawal(btcAmount: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                thunderDomeRepository.estimateWithdrawal(
                        WithdrawalRequest(
                                amount = btcAmount,
                                address = fundingModel.nextReceiveAddress
                        )
                )?.let { ledgerInvoice ->
                    lightningWithdrawalNetworkFee.postValue(ledgerInvoice.networkFeeCurrency)
                    lightningWithdrawalDropbitFee.postValue(ledgerInvoice.processingFeeCurrency)
                }
            }
        }
    }

    fun processWithdrawal(withdrawalRequest: WithdrawalRequest) {
        viewModelScope.launch(Dispatchers.Main) {
            val success = withContext(Dispatchers.IO) {
                withdrawalRequest.address = fundingModel.nextReceiveAddress
                thunderDomeRepository.postWithdrawal(withdrawalRequest)
            }
            withContext(Dispatchers.Main) {
                lightningWithdrawalCompleted.value = success
            }
        }

    }

    fun fundMaxForUpgrade(address: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val data = withContext(Dispatchers.IO) {
                transactionFundingManager.buildFundedTransactionDataForUpgrade(
                        address, feesManager.currentFee()
                )
            }
            withContext(Dispatchers.Main) {
                transactionData.value = data
            }
        }
    }

    fun fundMax(address: String?) {
        fundMax(address, feesManager.currentFee())
    }

    fun fundMax(address: String?, fee: Double) {
        viewModelScope.launch(Dispatchers.Main) {
            val data = withContext(Dispatchers.IO) {
                transactionFundingManager.buildFundedTransactionData(
                        if (address.isNullOrEmpty()) null else address, fee
                )
            }
            withContext(Dispatchers.Main) {
                transactionData.value = data
            }
        }
    }

    fun lookupIdentityHash(hash: String, accountMode: AccountMode) {
        viewModelScope.launch {
            val lookupResult = withContext(Dispatchers.IO) {
                val response: Response<*> = cnApiClient.queryWalletAddress(hash, accountMode)
                if (response.isSuccessful) {
                    response.body().let { result ->
                        val results = result as List<AddressLookupResult>
                        if (results.isNotEmpty()) {
                            results[0]
                        } else {
                            AddressLookupResult()
                        }
                    }
                } else {
                    AddressLookupResult()
                }
            }

            withContext(Dispatchers.Main) {
                addressLookupResult.value = lookupResult
            }
        }
    }

    fun fundTransaction(address: String? = null, amount: Long = 0) {
        fundTransaction(address, amount, feesManager.currentFee())
    }

    fun fundTransaction(address: String? = null, amount: Long = 0, fee: Double) {
        viewModelScope.launch(Dispatchers.Main) {
            val data = withContext(Dispatchers.IO) {
                transactionFundingManager.buildFundedTransactionData(
                        if (address.isNullOrEmpty()) null else address, fee, amount
                )
            }
            withContext(Dispatchers.Main) {
                transactionData.value = data
            }
        }
    }

    fun fundTransactionForDropbit(amount: Long) {
        fundTransactionForDropbit(amount, feesManager.currentFee())
    }

    fun fundTransactionForDropbit(amount: Long, fee: Double) {
        fundTransaction(amount = amount, fee = fee)
    }

    fun estimateLightningPayment(encodedInvoice: String, amount: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            val ledgerInvoice = withContext(Dispatchers.IO) {
                thunderDomeRepository.estimatePayment(encodedInvoice, amount)
            }
            withContext(Dispatchers.Main) {
                ledgerInvoice?.let {
                    pendingLedgerInvoice.value = it
                }
            }
        }
    }

    fun performLightningPayment(encodedInvoice: String, amount: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            val paidLedgerInvoice = withContext(Dispatchers.IO) {
                thunderDomeRepository.pay(encodedInvoice, amount)
            }
            withContext(Dispatchers.Main) {
                paidLedgerInvoice?.let {
                    ledgerInvoice.value = it
                }
            }
        }
    }

    fun performContactInvite(paymentHolder: PaymentHolder) {
        viewModelScope.launch {
            val invitedContact: InvitedContact = inviteContact(paymentHolder)
            withContext(Dispatchers.Main) {
                invitedContactResponse.value = invitedContact
            }
        }
    }

    private suspend fun inviteContact(paymentHolder: PaymentHolder) = withContext(Dispatchers.IO) {
        val invite = inviteTransactionSummaryHelper.saveTemporaryInvite(paymentHolder)
        if (invite == null) {
            InvitedContact()
        } else {
            val payload = InviteUserPayload(
                    Amount(paymentHolder.crypto.toLong(), paymentHolder.fiat.toLong()),
                    getSenderForIdentityInvite(invite.fromUser),
                    getReceiverFromIdentity(invite.toUser),
                    paymentHolder.requestId,
                    address_type = if (paymentHolder.accountMode == AccountMode.LIGHTNING) "lightning" else "btc"
            )

            val response = cnApiClient.inviteUser(payload)

            val invitedContact = if (response.isSuccessful) {
                handleSuccessfulInvite(response, invite)
            } else {
                InvitedContact()
            }
            invitedContact
        }
    }

    private fun handleSuccessfulInvite(response: Response<InvitedContact>, invite: InviteTransactionSummary): InvitedContact {
        val inviteResponse = response.body()
        if (inviteResponse == null) {
            return InvitedContact()
        } else {
            val acknowledgedInvite = inviteTransactionSummaryHelper.acknowledgeSentInvite(invite, inviteResponse.id)
            // TODO save shared memo
            if (invite.type == Type.LIGHTNING_SENT) {
                thunderDomeRepository.createSettlementForInvite(
                        acknowledgedInvite.id,
                        acknowledgedInvite.toUser.id,
                        acknowledgedInvite.fromUser.id,
                        acknowledgedInvite.sentDate
                )
            }
            return inviteResponse
        }
    }

    private fun getReceiverFromIdentity(userIdentity: UserIdentity): Receiver {
        val type = userIdentity.type
        var value = userIdentity.identity
        val handle = userIdentity.handle

        if (type == IdentityType.PHONE) {
            val phoneNumber = PhoneNumber(value)
            value = "${phoneNumber.countryCode}${phoneNumber.nationalNumber}"
        }

        return Receiver(type.asString(), value, handle)
    }


    private fun getSenderForIdentityInvite(identity: UserIdentity): Sender {
        val type = identity.type
        var value = identity.identity
        var handle: String? = null

        if (type == IdentityType.PHONE) {
            val phoneNumber = PhoneNumber(value)
            value = "${phoneNumber.countryCode}${phoneNumber.nationalNumber}"
        } else {
            handle = identity.handle
        }

        return Sender(type.asString(), value, handle)
    }
}