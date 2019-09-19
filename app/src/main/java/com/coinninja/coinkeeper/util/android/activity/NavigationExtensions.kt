package com.coinninja.coinkeeper.util.android.activity

import com.coinninja.coinkeeper.ui.base.BaseActivity


fun BaseActivity.goHome() {
    activityNavigationUtil.navigateToHome(this)
}

fun BaseActivity.viewRecoveryWords() {
    activityNavigationUtil.navigateToBackupRecoveryWords(this)
}

