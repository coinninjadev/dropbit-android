package com.coinninja.coinkeeper.interfaces

interface ErrorLogging {
    fun logError(error: Throwable)
}