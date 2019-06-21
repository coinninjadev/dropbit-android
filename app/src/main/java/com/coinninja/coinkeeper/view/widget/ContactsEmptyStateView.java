package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.util.CallbackHandler;

public class ContactsEmptyStateView extends ConstraintLayout {

    private CallbackHandler allowContactAccessCallback;

    public ContactsEmptyStateView(Context context) {
        this(context, null);
    }

    public ContactsEmptyStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContactsEmptyStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.merge_contacts_empty_state, this, true);
        findViewById(R.id.allow_contact_access_button).setOnClickListener(v -> allowContactAccessClicked());
    }

    public void setAllowAccessOnClickListener(CallbackHandler callbackHandler) {
        allowContactAccessCallback = callbackHandler;
    }

    private void allowContactAccessClicked() {
        allowContactAccessCallback.callback();
    }
}
