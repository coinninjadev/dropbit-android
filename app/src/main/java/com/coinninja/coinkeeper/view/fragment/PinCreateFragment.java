package com.coinninja.coinkeeper.view.fragment;

import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.view.edittext.PinEditText;
import com.coinninja.coinkeeper.view.fragment.abstracts.PinFragment;

public class PinCreateFragment extends PinFragment {

    @Override
    public void onResume() {
        super.onResume();

        initTextView(getRootView());
    }

    private void initTextView(View rootView) {
        TextView bodyDisplay = rootView.findViewById(R.id.headline);

        bodyDisplay.setText(getString(R.string.set_pin_headline));
    }

    @Override
    public void onSixDigits() {
        PinEditText txtPinEntry = getPinEditText();
        int[] userPin = txtPinEntry.getPin();

        txtPinEntry.clearPin();
        presenter.pinEntered_New(userPin);
    }
}
