package com.coinninja.coinkeeper.ui.segwit

import android.os.Bundle
import android.widget.Button
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.android.activity.goHome
import com.coinninja.coinkeeper.util.android.activity.viewRecoveryWords

class UpgradeToSegwitCompleteActivity : BaseActivity() {

    val viewWalletButton: Button get() = findViewById(R.id.view_wallet)
    val viewRecoveryWords: Button get() = findViewById(R.id.view_recovery_words)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrade_to_segwit_step_3)
        viewWalletButton.setOnClickListener { goHome() }
        viewRecoveryWords.setOnClickListener { viewRecoveryWords() }
    }

    override fun onBackPressed() = goHome()

}
