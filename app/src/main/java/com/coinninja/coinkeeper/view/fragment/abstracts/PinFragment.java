package com.coinninja.coinkeeper.view.fragment.abstracts;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.presenter.fragment.PinFragmentPresenter;
import com.coinninja.coinkeeper.ui.base.BaseFragment;
import com.coinninja.coinkeeper.view.edittext.PinEditText;

import androidx.annotation.Nullable;

public abstract class PinFragment extends BaseFragment implements PinEditText.OnSixDigitsEnteredListener, PinEditText.OnDismissRequestListener {

    private View rootView;
    protected PinEditText txtPinEntry;
    protected PinFragmentPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pin, container, false);

        txtPinEntry = rootView.findViewById(R.id.pin_entry_edittext);
        txtPinEntry.setOnSixDigitsEnteredListener(this);
        txtPinEntry.requestFocus();

        getPinEditText().setOnDismissRequestListener(this);

        forceShowSoftKey();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        txtPinEntry.clearPin();
        forceShowSoftKey();
    }

    @Override
    public void onPause() {
        super.onPause();

        txtPinEntry.clearPin();
    }

    public void forceShowSoftKey() {
        Handler runOnUI = new Handler(Looper.getMainLooper());
        runOnUI.postDelayed(() -> {
            if (txtPinEntry == null) {
                return;
            }
            txtPinEntry.requestFocus();
            if (this == null || getActivity() == null) {
                return;
            }
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(txtPinEntry, InputMethodManager.SHOW_IMPLICIT);
        }, 500);
    }

    public void setPresenter(PinFragmentPresenter presenter) {
        this.presenter = presenter;
    }

    public PinEditText getPinEditText() {
        return txtPinEntry;
    }

    public View getRootView() {
        return rootView;
    }

    @Override
    public void onDismissRequest() {
        int index = getActivity().getFragmentManager().getBackStackEntryCount();
        if (index > 0) {
            getActivity().getFragmentManager().popBackStack();
        } else {
            Activity activity = getActivity();
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
