package com.coinninja.coinkeeper.view.button

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.DrawableCompat
import com.coinninja.coinkeeper.R

class HelpLinkButton @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatButton(context, attrs, defStyleAttr) {

    var uri: Uri = Uri.EMPTY
    var color: Int = -1

    init {
        super.setOnClickListener {
            context?.let {
                if (uri.toString().isNotEmpty()) {
                    Intent(Intent.ACTION_VIEW, uri).also {
                        context.startActivity(it)
                    }
                }
            }
        }

        context?.let { _context ->
            _context.obtainStyledAttributes(attrs, R.styleable.HelpLinkButton, defStyleAttr, 0)?.let { typedArray ->
                if (typedArray.hasValue(R.styleable.HelpLinkButton_url)) {
                    typedArray.getResourceId(R.styleable.HelpLinkButton_url, -1).let {
                        if (it != -1) {
                            uri = Uri.parse(_context.getString(it))
                        } else {
                            typedArray.getString(R.styleable.HelpLinkButton_url)?.let {
                                uri = Uri.parse(it)
                            }
                        }
                    }
                }
                typedArray.recycle()
            }

            textAlignment = View.TEXT_ALIGNMENT_CENTER
            background = AppCompatResources.getDrawable(context, R.drawable.question_mark_circle)
            text = _context.getString(R.string.default_help_link_label)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {}

}