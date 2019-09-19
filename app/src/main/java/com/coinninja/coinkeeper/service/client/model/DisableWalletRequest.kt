package com.coinninja.coinkeeper.service.client.model

import com.coinninja.coinkeeper.cn.wallet.WalletFlags


data class DisableWalletRequest(val flags: Long = WalletFlags.purpose49v1Disabled)