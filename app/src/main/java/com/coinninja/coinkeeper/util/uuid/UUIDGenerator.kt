package com.coinninja.coinkeeper.util.uuid

import app.dropbit.annotations.Mockable
import java.util.*
import javax.inject.Inject

@Mockable
class UUIDGenerator @Inject internal constructor() {
    fun generate(): String {
        return UUID.randomUUID().toString()
    }
}