package com.coinninja.coinkeeper.cn.transaction.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.persistance.model.LightningInvoice
import app.coinninja.cn.thunderdome.model.LedgerInvoice
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.cn.wallet.tx.FundingModel
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult
import com.coinninja.coinkeeper.util.FeesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

@Suppress("UNCHECKED_CAST")
@Mockable
class FundingViewModel : ViewModel() {
    lateinit var transactionFundingManager: TransactionFundingManager
    lateinit var thunderDomeRepository: ThunderDomeRepository
    lateinit var feesManager: FeesManager
    lateinit var fundingModel: FundingModel
    lateinit var cnApiClient: SignedCoinKeeperApiClient

    var transactionData: MutableLiveData<TransactionData> = MutableLiveData()
    var lightningWithdrawalDropbitFee: MutableLiveData<BTCCurrency> = MutableLiveData()
    var lightningWithdrawalNetworkFee: MutableLiveData<BTCCurrency> = MutableLiveData()
    var lightningWithdrawalCompleted: MutableLiveData<Boolean> = MutableLiveData()
    var addressLookupResult: MutableLiveData<AddressLookupResult> = MutableLiveData()
    var pendingLedgerInvoice: MutableLiveData<LedgerInvoice> = MutableLiveData()

    fun clear() {
        transactionData = MutableLiveData()
        lightningWithdrawalDropbitFee = MutableLiveData()
        lightningWithdrawalCompleted = MutableLiveData()
        lightningWithdrawalNetworkFee = MutableLiveData()
        addressLookupResult = MutableLiveData()
        pendingLedgerInvoice = MutableLiveData()
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
                                amount = BTCCurrency(btcAmount),
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
                transactionFundingManager.buildFundedTransactionData(
                        address, feesManager.currentFee()
                )
            }
            withContext(Dispatchers.Main) {
                transactionData.value = data
            }
        }
    }

    fun fundMax(address: String?) {
        viewModelScope.launch(Dispatchers.Main) {
            val data = withContext(Dispatchers.IO) {
                transactionFundingManager.buildFundedTransactionData(
                        address, feesManager.currentFee()
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
        viewModelScope.launch(Dispatchers.Main) {
            val data = withContext(Dispatchers.IO) {
                transactionFundingManager.buildFundedTransactionData(
                        address, feesManager.currentFee(), amount
                )
            }
            withContext(Dispatchers.Main) {
                transactionData.value = data
            }
        }
    }

    fun fundTransactionForDropbit(amount: Long) {
        fundTransaction(amount = amount)
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
}