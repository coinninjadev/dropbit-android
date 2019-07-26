package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable

@Mockable
data class GsonAddress(var address: String = "", var txid: String = "", var derivationIndex: Int = 0)
