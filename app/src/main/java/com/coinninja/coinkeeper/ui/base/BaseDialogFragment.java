package com.coinninja.coinkeeper.ui.base;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.coinninja.coinkeeper.util.analytics.Analytics;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.CallSuper;
import dagger.android.AndroidInjection;

public class BaseDialogFragment extends DialogFragment {
    @Inject
    Analytics analytics;

    @CallSuper
    @Override
    public void onAttach(Context context) {
        AndroidInjection.inject(this);
        super.onAttach(context);
    }

    public Analytics getAnalytics() {
        return analytics;
    }

    @Override
    public void onStop() {
        super.onStop();
        analytics.trackFragmentStop(this);
    }

    protected void forceDropKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
