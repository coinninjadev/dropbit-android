package com.coinninja.coinkeeper.di.interfaces

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import javax.inject.Qualifier

@Qualifier
@Retention(RetentionPolicy.CLASS)
annotation class ThreadHandler