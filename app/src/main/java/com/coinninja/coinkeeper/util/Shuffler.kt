package com.coinninja.coinkeeper.util

import app.dropbit.annotations.Mockable
import java.util.*
import javax.inject.Inject

@Mockable
class Shuffler @Inject constructor() {
    fun shuffle(items: List<*>?) {
        Collections.shuffle(items)
    }

    fun pick(numChoices: Int): Int {
        return Random().nextInt(numChoices)
    }
}