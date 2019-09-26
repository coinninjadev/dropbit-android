package com.coinninja.coinkeeper.ui.memo

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.isNotNullOrEmpty
import com.coinninja.android.helpers.Input.showKeyboard
import com.coinninja.coinkeeper.R.id
import com.coinninja.coinkeeper.R.layout
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import javax.inject.Inject

@Mockable
class MemoCreator @Inject internal constructor() {

    @JvmOverloads
    fun createMemo(activity: AppCompatActivity, callback: OnMemoCreatedCallback, text: String = "") {
        val view = LayoutInflater.from(activity).inflate(layout.dialog_create_memo, null)
        GenericAlertDialog.newInstance(view, false, false).also { genericAlertDialog ->
            genericAlertDialog.asWide()
            view.also {
                val memoView: EditText = it.findViewById(id.memo)
                memoView.setText(text)
                memoView.setSelection(text.length)
                it.findViewById<View>(id.done).setOnClickListener {
                    val text = memoView.text.toString().trim { it <= ' ' }
                    if (text.isNotNullOrEmpty()) callback.onMemoCreated(text)
                    genericAlertDialog.dismiss()
                }
                it.postDelayed({ showKeyboard(memoView) }, 200)
            }
        }.show(activity.supportFragmentManager, MemoCreator::class.java.simpleName)

    }

    interface OnMemoCreatedCallback {
        fun onMemoCreated(memo: String)
    }
}