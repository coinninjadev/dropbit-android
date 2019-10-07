package com.coinninja.coinkeeper.ui.payment.invite

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import app.coinninja.cn.persistance.model.BTCState
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.android.helpers.styleAsLightning
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.service.client.model.InvitedContact
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.view.progress.SendingProgressView
import javax.inject.Inject


class InviteContactActivity : BaseActivity() {

    @Inject
    lateinit var fundingViewModelProvider: FundingViewModelProvider

    lateinit var fundingViewModel: FundingViewModel
    lateinit var paymentHolder: PaymentHolder

    val invitedContactResponseObserver: Observer<InvitedContact> = Observer { invitedContact ->
        when (BTCState.from(invitedContact.status)) {
            BTCState.UNFULFILLED -> showSuccess()
            else -> showFailure()
        }
    }

    val sendingProgressView: SendingProgressView get() = findViewById(R.id.broadcast_sending_progress)
    val sendingProgressLabel: TextView get() = findViewById(R.id.broadcast_sending_progress_label)
    val transactionIdLabel: TextView get() = findViewById(R.id.transaction_id_label)
    val transactionIdLink: TextView get() = findViewById(R.id.transaction_id_link)
    val transactionActionBtn: Button get() = findViewById(R.id.transaction_complete_action_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast)
        paymentDataFromCreation()
        fundingViewModel = fundingViewModelProvider.provide(this)
        retry()
    }

    override fun onResume() {
        super.onResume()
        fundingViewModel.invitedContactResponse.observe(this, invitedContactResponseObserver)
    }

    override fun onPause() {
        super.onPause()
        fundingViewModel.invitedContactResponse.removeObserver(invitedContactResponseObserver)
    }

    private fun clear() {
        sendingProgressView.resetView()
        sendingProgressLabel.show()
        sendingProgressLabel.text = resources.getText(R.string.broadcast_sent_label)
        transactionIdLabel.gone()
        transactionIdLink.gone()
        transactionActionBtn.gone()
        transactionActionBtn.setOnClickListener {}
    }

    private fun showSuccess() {
        sendingProgressView.progress = 100
        sendingProgressView.completeSuccess()
        sendingProgressLabel.show()
        sendingProgressLabel.text = resources.getText(R.string.broadcast_sent_label)
        transactionActionBtn.show()
        transactionActionBtn.text = resources.getText(R.string.broadcast_sent_ok)
        transactionActionBtn.styleAsLightning()
        transactionActionBtn.setOnClickListener { activityNavigationUtil.navigateToHome(this) }
        transactionIdLabel.text = getString(R.string.invite_sent_successfully)
    }

    internal fun showFailure() {
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
        clear()
        fundingViewModel.performContactInvite(paymentHolder)
    }

    private fun paymentDataFromCreation() {
        if (intent.hasExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER)) {
            paymentHolder = intent.getParcelableExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER)
        } else {
            paymentHolder = PaymentHolder()
            Toast.makeText(this, "Missing Payment Data", Toast.LENGTH_LONG).show()
            finish()
        }

        if (paymentHolder.cryptoCurrency.isZero) {
            Toast.makeText(this, "Missing Payment Data", Toast.LENGTH_LONG).show()
            finish()
        }
    }

}