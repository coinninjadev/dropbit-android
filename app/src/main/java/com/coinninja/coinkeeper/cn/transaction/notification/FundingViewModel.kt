package com.coinninja.coinkeeper.cn.transaction.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import com.coinninja.bindings.TransactionData
import com.coinninja.coinkeeper.cn.wallet.tx.FundingModel
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.util.FeesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Mockable
class FundingViewModel : ViewModel() {
    lateinit var transactionFundingManager: TransactionFundingManager
    lateinit var thunderDomeRepository: ThunderDomeRepository
    lateinit var feesManager: FeesManager
    lateinit var fundingModel: FundingModel

    val transactionData: MutableLiveData<TransactionData> = MutableLiveData()
    val lightningWithdrawalDropbitFee: MutableLiveData<BTCCurrency> = MutableLiveData()
    val lightningWithdrawalNetworkFee: MutableLiveData<BTCCurrency> = MutableLiveData()
    val lightningWithdrawalCompleted: MutableLiveData<Boolean> = MutableLiveData()

    fun fundLightningDeposit(btcAmount: Long): LiveData<TransactionData> {
        GlobalScope.launch(Dispatchers.Main) {
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
        GlobalScope.launch(Dispatchers.Main) {
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
        GlobalScope.launch(Dispatchers.Main) {
            val success = withContext(Dispatchers.IO) {
                withdrawalRequest.address = fundingModel.nextReceiveAddress
                thunderDomeRepository.postWithdrawal(withdrawalRequest)
            }
            withContext(Dispatchers.Main) {
                lightningWithdrawalCompleted.value = success
            }
        }

    }
}