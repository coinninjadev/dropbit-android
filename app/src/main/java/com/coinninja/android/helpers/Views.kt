package com.coinninja.android.helpers


import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Vibrator
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton

fun Fragment.show() {
    this.view?.show()
}

fun Fragment.hide() {
    this.view?.gone()
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.disable() {
    this.isEnabled = false
}

fun View.enable() {
    this.isEnabled = true
}

fun View.shakeInError() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.shake_view)
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    startAnimation(animation)
    postDelayed({ vibrator.cancel() }, 250)
    val pattern = longArrayOf(25, 100, 25, 100)
    vibrator.vibrate(pattern, 0)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun CheckBox.uncheck() {
    this.isChecked = false
}

fun CheckBox.check() {
    this.isChecked = true
}

fun Button.styleAsBitcoin() {
    when (this.id) {
        R.id.send_btn -> background = ContextCompat.getDrawable(context, R.drawable.btc_payment_bar_send_button)
        R.id.request_btn -> background = ContextCompat.getDrawable(context, R.drawable.btc_payment_bar_request_button)
        else -> background = ContextCompat.getDrawable(context, R.drawable.btc_button)
    }
}

fun Button.styleAsLightning() {
    when (this.id) {
        R.id.send_btn -> background = ContextCompat.getDrawable(context, R.drawable.lightning_payment_bar_send_button)
        R.id.request_btn -> background = ContextCompat.getDrawable(context, R.drawable.lightning_payment_bar_request_button)
        else -> background = ContextCompat.getDrawable(context, R.drawable.lightning_button)
    }
}

fun ConfirmHoldButton.styleAsLightning() {
    this.progressDrawable = ResourcesCompat.getDrawable(
            this.context.resources,
            R.drawable.confirm_progress_bar_lightning,
            this.context.theme
    )
}

fun ConfirmHoldButton.styleAsBitcoin() {
    this.progressDrawable = ResourcesCompat.getDrawable(
            this.context.resources,
            R.drawable.confirm_progress_bar_bitcoin,
            this.context.theme
    )
}

object Views {


    fun <T : View> withId(activity: Activity, resourceId: Int): T? {
        return activity.findViewById(resourceId)
    }

    fun <T : View> withId(activity: AppCompatActivity, resourceId: Int): T? {
        return activity.findViewById(resourceId)
    }

    fun <T : View> withId(view: AlertDialog, resourceId: Int): T? {
        return view.findViewById(resourceId)
    }

    fun <T : View> withId(view: View, resourceId: Int): T? {
        return view.findViewById(resourceId)
    }

    fun clickOn(view: View, resourceId: Int) {
        clickOn(withId(view, resourceId))
    }

    fun clickOn(view: View?) {
        view?.performClick()
    }

    fun rotate(view: View) {
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.rotate)
        animation.repeatCount = Animation.INFINITE
        view.startAnimation(animation)
    }

    fun renderBTCIconOnCurrencyViewPair(context: Context, defaultCurrencies: DefaultCurrencies, primaryCurrencyView: TextView,
                                        primaryScale: Double, secondaryCurrencyView: TextView, secondaryScale: Double) {
        val drawable = defaultCurrencies.crypto.getSymbolDrawable(context)
        if (defaultCurrencies.primaryCurrency.isCrypto) {
            drawSymbol(primaryCurrencyView, secondaryCurrencyView, primaryScale, drawable)
        } else {
            drawSymbol(secondaryCurrencyView, primaryCurrencyView, secondaryScale, drawable)
        }
    }

    private fun drawSymbol(viewToDraw: TextView, viewToClear: TextView, scale: Double, drawable: Drawable) {
        drawable.setBounds(0, 0,
                (drawable.intrinsicWidth * scale).toInt(),
                (drawable.intrinsicHeight * scale).toInt())
        viewToDraw.setCompoundDrawables(drawable, null, null, null)
        viewToClear.setCompoundDrawables(null, null, null, null)
    }

    fun setCompondDrawableOnStart(view: TextView, drawableId: Int, scale: Float) {
        val drawable = ResourcesCompat.getDrawable(view.resources, drawableId, null)
        drawable!!.setBounds(0, 0,
                (drawable.intrinsicWidth * scale).toInt(),
                (drawable.intrinsicHeight * scale).toInt())
        view.setCompoundDrawables(drawable, null, null, null)
    }

    fun clearCompoundDrawablesOn(view: TextView) {
        view.setCompoundDrawables(null, null, null, null)
    }
}
