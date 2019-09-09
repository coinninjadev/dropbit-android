package com.coinninja.coinkeeper.ui.lightning.withdrawal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.coinkeeper.view.progress.SendingProgressView
import javax.inject.Inject

class LightningWithdrawalBroadcastActivity : BaseActivity() {

    @Inject
    lateinit var fundingViewModelProvider: FundingViewModelProvider
    lateinit var fundingViewModel: FundingViewModel
    var withdrawalRequest = WithdrawalRequest()

    val onCompleteObserver: Observer<Boolean> = Observer { isSuccess ->
        if (isSuccess) {
            showSuccess()
        } else {
            showFailure()
        }
    }

    val sendingProgressView: SendingProgressView get() = findViewById(R.id.broadcast_sending_progress)
    val sendingProgressLabel: TextView get() = findViewById(R.id.broadcast_sending_progress_label)
    val transactionIdLabel: TextView get() = findViewById(R.id.transaction_id_label)
    val transactionIdLink: TextView get() = findViewById(R.id.transaction_id_link)
    val transactionIdIcon: ImageView get() = findViewById(R.id.transaction_id_link_image)
    val transactionActionBtn: Button get() = findViewById(R.id.transaction_complete_action_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast)
        fundingViewModel = fundingViewModelProvider.provide(this)
        fundingViewModel.lightningWithdrawalCompleted.observe(this, onCompleteObserver)
        clear()

        if (intent.hasExtra(DropbitIntents.EXTRA_WITHDRAWAL_REQUEST)) {
            withdrawalRequest = intent.getParcelableExtra(DropbitIntents.EXTRA_WITHDRAWAL_REQUEST)
            retry()
        } else {
            GenericAlertDialog.newInstance("Could not process withdrawal, missing withdrawal data").show(supportFragmentManager, "INVALID")
        }

    }

    private fun clear() {
        sendingProgressView.resetView()
        sendingProgressLabel.visibility = View.VISIBLE
        sendingProgressLabel.text = resources.getText(R.string.broadcast_sent_label)
        transactionIdLabel.visibility = View.GONE
        transactionIdLink.visibility = View.GONE
        transactionIdIcon.visibility = View.GONE
        transactionActionBtn.visibility = View.GONE
        transactionActionBtn.setOnClickListener { activityNavigationUtil.navigateToHome(this) }
    }

    private fun showSuccess() {
        sendingProgressView.progress = 100
        sendingProgressView.completeSuccess()
        sendingProgressLabel.visibility = View.VISIBLE
        sendingProgressLabel.text = resources.getText(R.string.broadcast_sent_label)
        transactionActionBtn.visibility = View.VISIBLE
        transactionActionBtn.text = resources.getText(R.string.broadcast_sent_ok)
        transactionActionBtn.background = ResourcesCompat.getDrawable(resources, R.drawable.lightning_button, theme)
        transactionActionBtn.setOnClickListener { activityNavigationUtil.navigateToHome(this) }
    }

    private fun showFailure() {
        sendingProgressView.progress = 100
        sendingProgressView.completeFail()
        sendingProgressLabel.visibility = View.VISIBLE
        sendingProgressLabel.text = resources.getText(R.string.broadcast_sent_label)
        transactionActionBtn.visibility = View.VISIBLE
        transactionActionBtn.text = resources.getText(R.string.broadcast_sent_try_again)
        transactionActionBtn.background = ResourcesCompat.getDrawable(resources, R.drawable.error_button, theme)
        transactionActionBtn.setOnClickListener { retry() }
    }

    private fun retry() {
        fundingViewModel.processWithdrawal(withdrawalRequest)
        clear()
    }
}
