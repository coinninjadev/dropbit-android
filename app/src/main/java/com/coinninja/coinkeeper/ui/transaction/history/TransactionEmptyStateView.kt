package com.coinninja.coinkeeper.ui.transaction.history

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.util.OnViewClickListener

class TransactionEmptyStateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var getBitcoinButtonClickListener: OnViewClickListener? = null
    private var learnBitcoinButtonClickListener: OnViewClickListener? = null
    private var spendBitcoinButtonClickListener: OnViewClickListener? = null

    init {
        init(context)
    }

    fun setGetBitcoinButtonClickListener(getBitcoinButtonClickListener: OnViewClickListener) {
        this.getBitcoinButtonClickListener = getBitcoinButtonClickListener
    }

    fun setLearnBitcoinButtonClickListener(learnBitcoinButtonClickListener: OnViewClickListener) {
        this.learnBitcoinButtonClickListener = learnBitcoinButtonClickListener
    }

    fun setSpendBitcoinButtonClickListener(spendBitcoinButtonClickListener: OnViewClickListener) {
        this.spendBitcoinButtonClickListener = spendBitcoinButtonClickListener
    }

    fun setupUIForWallet(numTransactions: Int, walletBalance: Long) {
        if (numTransactions >= 2) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            if (walletBalance > 0) {
                findViewById<View>(R.id.no_bitcoin_yet_title).visibility = View.GONE
                findViewById<View>(R.id.bitcoin_detail_text_view).visibility = View.GONE
                findViewById<View>(R.id.spend_bitcoin_button).visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.no_bitcoin_yet_title).visibility = View.VISIBLE
                findViewById<View>(R.id.bitcoin_detail_text_view).visibility = View.VISIBLE
                findViewById<View>(R.id.spend_bitcoin_button).visibility = View.GONE
            }
        }
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.merge_transaction_empty_state, this, true)
        orientation = LinearLayout.VERTICAL

        val getBitcoinButton = findViewById<Button>(R.id.get_bitcoin_button)
        getBitcoinButton.setOnClickListener { this.onGetBitcoinClicked(it) }

        val learnBitcoinButton = findViewById<Button>(R.id.learn_bitcoin_button)
        learnBitcoinButton.setOnClickListener { this.learnBitcoinClicked(it) }

        val spendBitcoinButton = findViewById<Button>(R.id.spend_bitcoin_button)
        spendBitcoinButton.setOnClickListener { this.spendBitcoinClicked(it) }
    }

    private fun learnBitcoinClicked(view: View) {
        learnBitcoinButtonClickListener?.onViewClicked(view)
    }

    private fun spendBitcoinClicked(view: View) {
        spendBitcoinButtonClickListener?.onViewClicked(view)
    }

    private fun onGetBitcoinClicked(view: View) {
        getBitcoinButtonClickListener?.onViewClicked(view)
    }

}
