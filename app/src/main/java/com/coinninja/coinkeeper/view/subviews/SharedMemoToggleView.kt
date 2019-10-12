package com.coinninja.coinkeeper.view.subviews

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.coinkeeper.R.id
import com.coinninja.coinkeeper.ui.memo.MemoCreator
import com.coinninja.coinkeeper.ui.memo.MemoCreator.OnMemoCreatedCallback
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import javax.inject.Inject

class SharedMemoToggleView @Inject internal constructor(
        internal val activityNavigationUtil: ActivityNavigationUtil,
        internal val memoCreator: MemoCreator
) : ActivityViewType {

    internal var view: View? = null
    internal var memoView: TextView? = null
    internal var shareToggleButton: ImageView? = null
    internal var unShareToggleButton: ImageView? = null
    var isSharing = true
        private set

    override fun render(activity: AppCompatActivity, rootView: View) {
        view = rootView
        memoView = view?.findViewById<TextView?>(id.memo_text_view)
        shareToggleButton = view?.findViewById<ImageView?>(id.shared_memo_toggle_button)
        unShareToggleButton = view?.findViewById<ImageView?>(id.unshare_memo_toggle_button)
        view?.findViewById<View>(id.shared_memo_tooltip_button)?.setOnClickListener { activityNavigationUtil.explainSharedMemos(activity) }
        view?.findViewById<View>(id.memo_text_view)?.setOnClickListener { textView ->
            val memoView: TextView = textView as TextView
            memoCreator.createMemo(activity, object : OnMemoCreatedCallback {
                override fun onMemoCreated(memo: String) {
                    if (memo.isEmpty()) return
                    memoView.text = memo
                }
            }, memoView.text.toString())
        }
        view?.findViewById<View>(id.memo_background_view)?.setOnClickListener { toggleSharingMemo() }
        showSharedMemoViews()
    }

    override fun tearDown() {
        view?.findViewById<View>(id.shared_memo_tooltip_button)?.setOnClickListener(null)
        view?.findViewById<View>(id.memo_text_view)?.setOnClickListener(null)
        view?.findViewById<View>(id.memo_background_view)?.setOnClickListener(null)
        view = null
        memoView = null
        shareToggleButton = null
        unShareToggleButton = null
    }

    fun setText(text: String?) {
        memoView?.text = text ?: ""
    }

    fun hideSharedMemoViews() {
        isSharing = false
        view?.findViewById<View>(id.shared_memo_group)?.gone()
        shareToggleButton?.gone()
        unShareToggleButton?.gone()
    }

    fun showSharedMemoViews() {
        isSharing = true
        view?.findViewById<View>(id.shared_memo_group)?.show()
        updateSharingViews()
    }

    val memo: String get() = memoView?.text.toString()

    fun toggleSharingMemo() {
        isSharing = !isSharing
        updateSharingViews()
    }

    private fun updateSharingViews() {
        shareToggleButton?.visibility = if (isSharing) View.VISIBLE else View.GONE
        unShareToggleButton?.visibility = if (isSharing) View.GONE else View.VISIBLE
    }


}