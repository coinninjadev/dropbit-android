package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.util.OnViewClickListener;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TransactionEmptyStateView extends ConstraintLayout {

    private Button getBitcoinButton;
    private Button learnBitcoinButton;
    private Button spendBitcoinButton;

    private OnViewClickListener getBitcoinButtonClickListener;
    private OnViewClickListener learnBitcoinButtonClickListener;
    private OnViewClickListener spendBitcoinButtonClickListener;

    public TransactionEmptyStateView(Context context) {
        this(context, null);
    }

    public TransactionEmptyStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransactionEmptyStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setGetBitcoinButtonClickListener(OnViewClickListener getBitcoinButtonClickListener) {
        this.getBitcoinButtonClickListener = getBitcoinButtonClickListener;
    }

    public void setLearnBitcoinButtonClickListener(OnViewClickListener learnBitcoinButtonClickListener) {
        this.learnBitcoinButtonClickListener = learnBitcoinButtonClickListener;
    }

    public void setSpendBitcoinButtonClickListener(OnViewClickListener spendBitcoinButtonClickListener) {
        this.spendBitcoinButtonClickListener = spendBitcoinButtonClickListener;
    }

    public void setupUIForWallet(WalletHelper walletHelper) {
        if (walletHelper.getTransactionsLazily().size() >= 2) {
            findViewById(R.id.empty_transaction_history).setVisibility(View.GONE);
        } else {
            findViewById(R.id.empty_transaction_history).setVisibility(View.VISIBLE);

            if (walletHelper.getBalance() > 0) {
                findViewById(R.id.no_bitcoin_yet_title).setVisibility(View.GONE);
                findViewById(R.id.bitcoin_detail_text_view).setVisibility(View.GONE);
                findViewById(R.id.spend_bitcoin_button).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.no_bitcoin_yet_title).setVisibility(View.VISIBLE);
                findViewById(R.id.bitcoin_detail_text_view).setVisibility(View.VISIBLE);
                findViewById(R.id.spend_bitcoin_button).setVisibility(View.GONE);
            }
        }
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.merge_transaction_empty_state, this, true);

        getBitcoinButton = findViewById(R.id.get_bitcoin_button);
        getBitcoinButton.setOnClickListener(this::onGetBitcoinClicked);

        learnBitcoinButton = findViewById(R.id.learn_bitcoin_button);
        learnBitcoinButton.setOnClickListener(this::learnBitcoinClicked);
        
        spendBitcoinButton = findViewById(R.id.spend_bitcoin_button);
        spendBitcoinButton.setOnClickListener(this::spendBitcoinClicked);
    }

    private void learnBitcoinClicked(View view) {
       learnBitcoinButtonClickListener.onViewClicked(view);
    }

    private void spendBitcoinClicked(View view) {
        spendBitcoinButtonClickListener.onViewClicked(view);
    }

    private void onGetBitcoinClicked(View view) {
        getBitcoinButtonClickListener.onViewClicked(view);
    }

}
