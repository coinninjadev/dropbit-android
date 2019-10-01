package com.coinninja.coinkeeper.ui.payment.confirm

import android.os.Bundle
import android.view.View
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.BaseActivity

class ConfirmPaymentActivity : BaseActivity() {

    val closeButton: View get() = findViewById(R.id.close_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_payment)
        closeButton.setOnClickListener { activityNavigationUtil.navigateToHome(this) }
    }

}
