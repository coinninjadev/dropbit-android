package com.coinninja.coinkeeper.view.fragment;

import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.view.fragment.abstracts.PinFragment;

public class PinConfirmFragment extends PinFragment {

    private TextView pinErrorDisplay;


    @Override
    public void onResume() {
        super.onResume();
        View rootView = getRootView();

        initTextView(rootView);
    }

    private void initTextView(View rootView) {
        TextView bodyDisplay = rootView.findViewById(R.id.headline);
        pinErrorDisplay = rootView.findViewById(R.id.error_message);

        bodyDisplay.setText(getString(R.string.pin_body_confirm));
        ((BaseActivity) this.getActivity()).updateActivityLabel(getString(R.string.pin_header_confirm));

        pinErrorDisplay.setText("");
        pinErrorDisplay.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSixDigits() {
        try {
            int[] userPinEntry = txtPinEntry.getPin();
            presenter.pinEntered_Confirm(userPinEntry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroyPinConfirm();
    }

    public void showPinMismatch() {
        pinErrorDisplay.setText(getResources().getText(R.string.pin_mismatch_error));
        pinErrorDisplay.setVisibility(View.VISIBLE);
        txtPinEntry.onError();
    }
}
