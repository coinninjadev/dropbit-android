package com.coinninja.coinkeeper.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.coinninja.android.helpers.styleAsBitcoin
import com.coinninja.android.helpers.styleAsLightning
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.R.layout
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode

class PaymentBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    var scanPressedObserver: OnScanPressedObserver? = null
    var requestPressedObserver: OnRequestPressedObserver? = null
    var sendPressedObserver: OnSendPressedObserver? = null

    val scanButton: Button get() = findViewById(R.id.scan_btn)
    val sendButton: Button get() = findViewById(R.id.send_btn)
    val requestButton: Button get() = findViewById(R.id.request_btn)
    var accountMode: AccountMode = AccountMode.BLOCKCHAIN
        set(value) {
            field = value
            invalidateButtonSkins()
        }

    private fun invalidateButtonSkins() {
        when (accountMode) {
            AccountMode.LIGHTNING -> {
                findViewById<Button>(R.id.send_btn).styleAsLightning()
                findViewById<Button>(R.id.request_btn).styleAsLightning()
            }
            else -> {
                findViewById<Button>(R.id.send_btn).styleAsBitcoin()
                findViewById<Button>(R.id.request_btn).styleAsBitcoin()
            }
        }

    }

    fun setOnRequestPressedObserver(requestPressedObserver: OnRequestPressedObserver?) {
        this.requestPressedObserver = requestPressedObserver
    }

    fun setOnSendPressedObserver(sendPressedObserver: OnSendPressedObserver?) {
        this.sendPressedObserver = sendPressedObserver
    }

    fun setOnScanPressedObserver(scanPressedObserver: OnScanPressedObserver?) {
        this.scanPressedObserver = scanPressedObserver
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(layout.merge_component_payment_bar, this, true)
        scanButton.setOnClickListener { scanPressedObserver?.onScanPressed() }
        sendButton.setOnClickListener { sendPressedObserver?.onSendPressed() }
        requestButton.setOnClickListener { requestPressedObserver?.onRequestPressed() }
    }


    interface OnSendPressedObserver {
        fun onSendPressed()
    }

    interface OnScanPressedObserver {
        fun onScanPressed()
    }

    interface OnRequestPressedObserver {
        fun onRequestPressed()
    }

    init {
        init(context)
    }
}