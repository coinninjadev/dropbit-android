package com.coinninja.coinkeeper.cn.wallet

import app.dropbit.annotations.Mockable
import com.coinninja.bindings.Libbitcoin
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope

import javax.inject.Inject

@Mockable
@CoinkeeperApplicationScope
class SeedWordGenerator @Inject constructor() {

    fun generate(): Array<String> {
        return Libbitcoin().createSeedWords
    }

}
