package com.coinninja.coinkeeper.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.coinninja.android.helpers.disable
import com.coinninja.android.helpers.enable
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode

class AccountModeToggleButton @JvmOverloads constructor(
        context: Context, attrs:
        AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context,
        attrs,
        defStyleAttr
) {

    var onModeSelectedObserver: AccountModeSelectedObserver? = null

    var mode: AccountMode = AccountMode.BLOCKCHAIN
        set(value) {
            field = value
            invalidateButtons()
        }

    var isLightningLocked = false
        set(value) {
            field = value
            if (value) gone() else show()
        }

    var active: Boolean = true
        set(value: Boolean) {
            field = value
            invalidateButtons()
        }

    internal val lightningButton: Button get() = findViewById(R.id.view_lightning)
    internal val blockchainButton: Button get() = findViewById(R.id.view_btc)

    init {
        LayoutInflater.from(context).inflate(R.layout.merge_component_account_mode_toggle_button, this, true)
        lightningButton.setOnClickListener { onModeSelectedObserver?.onSelectionChange(AccountMode.LIGHTNING) }
        blockchainButton.setOnClickListener { onModeSelectedObserver?.onSelectionChange(AccountMode.BLOCKCHAIN) }
        invalidateButtons()
    }

    private fun invalidateButtons() {
        when {
            mode == AccountMode.LIGHTNING && active -> {
                lightningButton.disable()
                blockchainButton.enable()
            }
            mode == AccountMode.LIGHTNING && !active -> {
                lightningButton.disable()
                blockchainButton.gone()
            }
            mode == AccountMode.BLOCKCHAIN && active -> {
                lightningButton.enable()
                blockchainButton.disable()
            }
            mode == AccountMode.BLOCKCHAIN && !active -> {
                lightningButton.gone()
                blockchainButton.disable()
            }
        }
    }


    interface AccountModeSelectedObserver {
        fun onSelectionChange(mode: AccountMode)
    }

}
