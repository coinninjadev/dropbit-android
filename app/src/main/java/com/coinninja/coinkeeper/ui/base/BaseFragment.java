package com.coinninja.coinkeeper.ui.base;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.coinninja.coinkeeper.util.analytics.Analytics;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class BaseFragment extends DaggerFragment {
    @Inject
    Analytics analytics;


    protected void forceDropKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
