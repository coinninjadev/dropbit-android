package com.coinninja.coinkeeper.ui.actionbar.managers


import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import app.dropbit.annotations.Mockable
import javax.inject.Inject

@Mockable
class TitleViewManager @Inject constructor() {
    //TODO make this A Constructor argument
    var actionBar: ActionBar? = null

    //TODO make this A Constructor argument
    var titleView: TextView? = null

    private var _title = ""

    var title: String
        get() {
            return if (_title.isNotEmpty()) {
                _title
            } else {
                _title = actionBar?.title?.toString() ?: ""
                actionBar?.title = ""
                _title
            }
        }
        set(value) {
            actionBar?.title = ""
            _title = value
        }

    fun renderTitle() {
        if (title.isNotEmpty()) {
            titleView?.visibility = View.VISIBLE
            titleView?.text = title.toUpperCase()
        } else {
            removeTitle()
        }
    }

    private fun removeTitle() {
        titleView?.visibility = View.GONE
    }
}
