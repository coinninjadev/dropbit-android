package com.coinninja.coinkeeper.cn.wallet.service

import android.os.Binder
import app.dropbit.annotations.Mockable

import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface

@Mockable
class CNWalletBinder(val service: CNWalletServicesInterface) : Binder()
