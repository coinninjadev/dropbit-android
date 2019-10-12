package com.coinninja.coinkeeper.view.activity

import android.content.*
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.service.SyncDropBitService
import com.coinninja.coinkeeper.text.TextInputNotifierWatcher
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import javax.inject.Inject

class VerifyPhoneVerificationCodeActivity : BaseActivity(), TextInputNotifierWatcher.OnInputEventListener, View.OnFocusChangeListener, DialogInterface.OnClickListener {

    @Inject
    internal lateinit var dropbitMeConfiguration: DropbitMeConfiguration
    @Inject
    internal lateinit var serviceWorkUtil: ServiceWorkUtil

    internal val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when {
                    DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE == intent.action -> {
                        onInvalidCode()
                        clearAll()
                    }
                    DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE == intent.action -> {
                        onExpiredCode()
                        clearAll()
                    }
                    DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR == intent.action -> {
                        onRateLimitErrorCode()
                        clearAll()
                    }
                    DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR == intent.action -> {
                        onServerErrorCode()
                        clearAll()
                    }
                    DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR == intent.action -> {
                        onServerBlacklistErrorCode()
                        clearAll()
                    }
                    DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT == intent.action -> {
                        onVerificationCodeSent()
                        clearAll()
                    }
                    DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS == intent.action -> {
                        onSuccess()
                        clearAll()
                    }
                    else -> {
                    }
                }
            }
        }

    }

    internal val intentFilter: IntentFilter = IntentFilter().also {
        it.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE)
        it.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE)
        it.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS)
        it.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR)
        it.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR)
        it.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR)
        it.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT)
    }

    private var watcher: TextInputNotifierWatcher = TextInputNotifierWatcher(this)

    private var errorCount = 0

    private val parent: ViewGroup? get() = findViewById(R.id.pin_wrapper)
    private val resendLink: TextView? get() = findViewById(R.id.resend_link)
    private val errorMessage: TextView? get() = findViewById(R.id.error_message)
    private var phoneNumber: PhoneNumber? = null

    private val code: String
        get() {
            parent?.let {
                val code = StringBuilder()
                for (i in 0 until it.childCount) {
                    val view = it.getChildAt(i)
                    val char = if (view == null) "" else (view as EditText).text.toString()
                    code.append(char)
                }
                return code.toString()
            }
            return ""
        }

    override fun onInput(numValues: Int) {
        parent?.let {
            hideErrors()
            val focusedChild = it.focusedChild as EditText
            val childIndex = it.indexOfChild(focusedChild)
            it.getChildAt(childIndex + 1)?.also { next ->
                when {
                    numValues == 1 -> focusOn(next as EditText)
                    numValues > 1 -> handlePaste(focusedChild)
                    else -> {
                    }
                }

            }

            if (code.length == 6)
                serviceWorkUtil.validatePhoneNumberConfirmationCode(code)
        }
    }

    override fun onRemove(numValues: Int) {
        parent?.let {
            val focusedChild = it.focusedChild as EditText
            it.getChildAt(it.indexOfChild(focusedChild) - 1)?.also { previous ->
                focusOn(previous as EditText)
            }
        }
    }

    override fun onAfterChanged(text: String) {
        // NA
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        parent?.let { viewGroup ->
            if (hasFocus) {
                val focusedIndex = viewGroup.indexOfChild(view)
                if (focusedIndex != 0) {
                    clearForward(focusedIndex)
                    addWatcher(view as EditText)
                    (viewGroup.getChildAt(focusedIndex - 1) as EditText).also { previous ->
                        if (previous.text.toString().isEmpty()) {
                            removeWatcher(view)
                            focusOn(previous)
                        }
                    }
                } else {
                    addWatcher(view as EditText)
                }
            } else {
                removeWatcher(view as EditText)
            }

        }
    }

    fun onInvalidCode() {
        clearAll()
        errorMessage?.visibility = View.VISIBLE
        errorCount += 1

        if (errorCount > 2) {
            hideErrors()
            notifyOfToManyAttempts()
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        onResendClick()
        dialog.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone_code)
        phoneNumber = intent.getParcelableExtra(DropbitIntents.EXTRA_PHONE_NUMBER)

        phoneNumber?.let {
            findViewById<TextView>(R.id.headline).apply {
                text = resources.getString(R.string.activity_verify_phone_code_headline, it.toInternationalDisplayText())
            }
        }

        resendLink?.setOnClickListener { onResendClick() }
        resendLink?.paintFlags = resendLink?.paintFlags ?: Paint.UNDERLINE_TEXT_FLAG
        setup()
    }

    override fun onStart() {
        super.onStart()
        parent?.getChildAt(0)?.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        localBroadCastUtil.unregisterReceiver(receiver)
    }

    override fun onStop() {
        super.onStop()
        clearAll()
    }

    override fun onResume() {
        super.onResume()
        localBroadCastUtil.registerReceiver(receiver, intentFilter)
    }

    private fun setup() = parent?.let { viewGroup ->
        for (i in 0 until viewGroup.childCount) {
            viewGroup.getChildAt(i).onFocusChangeListener = this
        }
    }

    internal fun addWatcher(view: EditText) {
        view.addTextChangedListener(watcher)
        view.setOnKeyListener(watcher)
    }

    private fun clearAll() = parent?.let {
        clearForward(0)
        focusOn(it.getChildAt(0) as EditText)
    }

    private fun clearForward(focusedIndex: Int) = parent?.let { viewGroup ->
        for (i in focusedIndex until viewGroup.childCount) {
            val text = viewGroup.getChildAt(i) as EditText
            if (text.text.toString().isNotEmpty()) {
                text.setText("")
            }
        }
    }

    internal fun focusOn(view: EditText) {
        view.setText("")
        view.isFocusable = true
        view.requestFocus()
        view.requestFocusFromTouch()
        view.onFocusChangeListener = this
    }

    private fun handlePaste(focusedChild: EditText) {
        parent?.let { viewGroup ->
            removeWatcher(focusedChild)
            val text = focusedChild.text.toString()
            clearAll()
            text.forEachIndexed { index, char ->
                if (index < 6) {
                    val view = viewGroup.getChildAt(index)
                    view?.let {
                        val textView = it as EditText
                        textView.removeTextChangedListener(watcher)
                        textView.setText(char.toString())
                    }
                }
            }
            if (text.length < 5) {
                viewGroup.getChildAt(text.length)?.let {
                    focusOn(it as EditText)
                }
            }
        }
    }

    private fun onResendClick() = phoneNumber?.let {
        serviceWorkUtil.resendPhoneVerification(it)
    }

    private fun onVerificationCodeSent() {
        GenericAlertDialog.newInstance(null,
                getString(R.string.activity_verify_code_sent),
                "ok", null,
                { dialog, which -> this.onVerificationCodeSentClickListener() },
                false,
                false
        ).show(supportFragmentManager, VERIFICATION_CODE_SENT)
    }

    private fun onVerificationCodeSentClickListener() {
        parent?.getChildAt(0)?.requestFocus()
    }

    private fun onExpiredCode() {
        GenericAlertDialog.newInstance(null,
                getString(R.string.activity_verify_phonecode_expired),
                "ok", null,
                this,
                false,
                false
        ).show(supportFragmentManager, EXPIRED_CODE_FRAGMENT_TAG)
    }

    private fun onRateLimitErrorCode() {
        GenericAlertDialog.newInstance(null,
                getString(R.string.activity_verify_phonecode_ratelimit),
                "ok", null, null,
                false,
                false
        ).show(supportFragmentManager, TOO_FAST_SERVER_ATTEMPTS_FRAGMENT_TAG)
    }

    private fun onServerErrorCode() {
        GenericAlertDialog.newInstance(null,
                getString(R.string.activity_verify_phonecode_error_server),
                "ok", null, null,
                false,
                false
        ).show(supportFragmentManager, SERVER_ERROR_FRAGMENT_TAG)
    }

    private fun onServerBlacklistErrorCode() {
        GenericAlertDialog.newInstance(null,
                getString(R.string.activity_verify_phonecode_error_server_blacklist),
                "ok", null, null,
                false,
                false
        ).show(supportFragmentManager, SERVER_ERROR_FRAGMENT_TAG)
    }

    private fun onSuccess() {
        if (analytics != null) {
            analytics.trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SUCCESSFUL)
        }
        startService(Intent(this, SyncDropBitService::class.java))
        dropbitMeConfiguration.setInitialVerification()
        activityNavigationUtil.navigateToHome(this)
    }

    private fun hideErrors() {
        errorMessage?.visibility = View.GONE
    }

    private fun notifyOfToManyAttempts() {
        GenericAlertDialog.newInstance(null,
                getString(R.string.verify_phone_code_too_many_tries),
                "OK", null,
                this, false, false
        ).show(supportFragmentManager, TOO_MANY_ATTEMPTS_FRAGMENT_TAG)
    }

    private fun removeWatcher(view: EditText) {
        view.removeTextChangedListener(watcher)
        view.setOnKeyListener(null)
    }

    companion object {
        internal const val EXPIRED_CODE_FRAGMENT_TAG = "EXPIRED_CODE_FRAGMENT_TAG"
        internal const val VERIFICATION_CODE_SENT = "VERIFICATION_CODE_SENT"
        internal const val TOO_MANY_ATTEMPTS_FRAGMENT_TAG = "TOO_MANY_ATTEMPTS_FRAGMENT_TAG"
        internal const val TOO_FAST_SERVER_ATTEMPTS_FRAGMENT_TAG = "TOO_FAST_SERVER_ATTEMPTS_FRAGMENT_TAG"
        internal const val SERVER_ERROR_FRAGMENT_TAG = "SERVER_ERROR_FRAGMENT_TAG"
    }
}
