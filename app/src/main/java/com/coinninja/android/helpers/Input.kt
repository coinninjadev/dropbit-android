package com.coinninja.android.helpers

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object Input {
    fun showKeyboard(view: EditText) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}
