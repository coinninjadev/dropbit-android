package com.coinninja.coinkeeper.view.subviews

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.dropbit.annotations.Mockable
import com.coinninja.android.helpers.gone
import com.coinninja.coinkeeper.R
import javax.inject.Inject

@Mockable
class SharedMemoView  @Inject constructor() {

    @JvmOverloads
    fun render(view: View, isSharing: Boolean, memoText: String = "", displayText: String?=null) {
        val sharedMemoStatusTextView = view.findViewById<TextView>(R.id.shared_memo_status_text_view)
        val sharedStatusImageView = view.findViewById<ImageView>(R.id.shared_status_image_view)

        if (isSharing) {
            sharedStatusImageView.setImageResource(R.drawable.ic_shared_user)
            sharedStatusImageView.tag = R.drawable.ic_shared_user
            sharedMemoStatusTextView.text = view.resources.getString(R.string.shared_memo, displayText?: "")
        } else {
            sharedStatusImageView.setImageResource(R.drawable.ic_single_user)
            sharedStatusImageView.tag = R.drawable.ic_single_user
            sharedMemoStatusTextView.setText(R.string.only_me_memo)
        }
        if (memoText.isEmpty()) {
            view.gone()
        } else {
            view.findViewById<TextView>(R.id.shared_memo_text_view).text = memoText
        }
    }

}