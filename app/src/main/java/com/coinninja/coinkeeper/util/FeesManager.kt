package com.coinninja.coinkeeper.util

import com.coinninja.coinkeeper.service.client.model.TransactionFee
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import javax.inject.Inject


open class FeesManager @Inject constructor(val preferencesUtil: PreferencesUtil) {

    companion object {
        const val FAST_FEE_STRING = "ADJUSTABLE_FEE_TYPE_FAST"
        const val SLOW_FEE_STRING = "ADJUSTABLE_FEE_TYPE_SLOW"
        const val CHEAP_FEE_STRING = "ADJUSTABLE_FEE_TYPE_CHEAP"
    }

    enum class FeeType(val type: String) {
        FAST(FAST_FEE_STRING),
        SLOW(SLOW_FEE_STRING),
        CHEAP(CHEAP_FEE_STRING)
    }

    var feePreference: FeeType
        get() {
            return when(preferencesUtil.getString(DropbitIntents.PREFERENCES_ADJUSTABLE_FEES_TYPE, FAST_FEE_STRING) ?: FAST_FEE_STRING) {
                FAST_FEE_STRING -> {
                    FeeType.FAST
                }
                SLOW_FEE_STRING -> {
                    FeeType.SLOW
                }
                CHEAP_FEE_STRING -> {
                    FeeType.CHEAP
                }
                else -> {
                    FeeType.FAST
                }
            }
        }
        set(feePreference) {
            preferencesUtil.savePreference(DropbitIntents.PREFERENCES_ADJUSTABLE_FEES_TYPE, feePreference.type)
        }

    var isAdjustableFeesEnabled: Boolean
        get() {
            return preferencesUtil.getBoolean(DropbitIntents.PREFERENCES_ADJUSTABLE_FEES_ENABLED, false)
        }
        set(isAdjustableFeesEnabled) {
            preferencesUtil.savePreference(DropbitIntents.PREFERENCES_ADJUSTABLE_FEES_ENABLED, isAdjustableFeesEnabled)
        }

    fun fees() : TransactionFee {
        return TransactionFee(fee(FeeType.CHEAP), fee(FeeType.SLOW), fee(FeeType.FAST))
    }

    fun currentFee() : Double {
        return fee(feePreference)
    }

    fun fee(type: FeeType) : Double {
        val fee = when(type) {
            FeeType.FAST -> {
                preferencesUtil.getString(FAST_FEE_STRING, "0.0") ?: "0.0"
            }
            FeeType.SLOW -> {
                preferencesUtil.getString(SLOW_FEE_STRING, "0.0") ?: "0.0"
            }
            FeeType.CHEAP -> {
                preferencesUtil.getString(CHEAP_FEE_STRING, "0.0") ?: "0.0"
            }
        }

        return fee.toDouble()
    }

    fun setFee(type: FeeType, amount: Double) {
        when(type) {
            FeeType.FAST -> {
                preferencesUtil.savePreference(FAST_FEE_STRING, amount.toString())
            }
            FeeType.SLOW -> {
                preferencesUtil.savePreference(SLOW_FEE_STRING, amount.toString())
            }
            FeeType.CHEAP -> {
                preferencesUtil.savePreference(CHEAP_FEE_STRING, amount.toString())
            }
        }
    }

    fun setFees(transactionFee: TransactionFee) {
        setFee(FeeType.FAST, transactionFee.fast)
        setFee(FeeType.SLOW, transactionFee.med)
        setFee(FeeType.CHEAP, transactionFee.slow)
    }
}