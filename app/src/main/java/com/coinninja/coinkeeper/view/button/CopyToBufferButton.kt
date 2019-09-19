package com.coinninja.coinkeeper.view.button

import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.coinninja.coinkeeper.R.string
import com.coinninja.coinkeeper.util.android.ClipboardUtil

class CopyToBufferButton : AppCompatButton {
    private var util: ClipboardUtil? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        util = ClipboardUtil(context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        super.setOnClickListener { v: View? -> copyClip() }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        return
    }

    private fun copyClip() {
        val clipLabel = clipLabel
        util!!.setClipFromText(clipLabel, clip)
        Toast.makeText(context, clipLabel, Toast.LENGTH_LONG).show()
    }

    private val clipLabel: String
        private get() = context.getString(string.copied, clip)

    private val clip: String
        private get() = asString()

    fun asString(): String {
        return text.toString()
    }
}