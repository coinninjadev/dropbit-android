package com.coinninja.android.helpers;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Input {
    public static void showKeyboard(EditText view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
