package com.coinninja.coinkeeper.text

import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import app.dropbit.annotations.Mockable
import com.google.i18n.phonenumbers.AsYouTypeFormatter
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.util.*


@Mockable
class PhoneNumberFormattingTextWatcher(
        internal var locale: Locale,
        internal val callback: Callback
) : TextWatcher {

    private val discardableValues: Map<String, String> = mapOf(
            Pair(" ", " "),
            Pair("(", ""),
            Pair(")", ""),
            Pair("-", "-")
    )

    private var selfChanged = false
    private var stopWatching = false
    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    private var phoneNumber: Phonenumber.PhoneNumber = Phonenumber.PhoneNumber()

    private lateinit var templateNumber: Phonenumber.PhoneNumber
    private lateinit var i18nMatcher: String
    private lateinit var ccMatcher: String
    private lateinit var formatter: AsYouTypeFormatter
    private lateinit var formattedTemplateNumber: String

    init {
        updateLocale(locale)
    }

    fun updateLocale(updatedLocale: Locale) {
        this.locale = updatedLocale
        templateNumber = phoneNumberUtil.getExampleNumberForType(updatedLocale.country,
                PhoneNumberUtil.PhoneNumberType.MOBILE)
        phoneNumber.countryCode = templateNumber.countryCode
        formatter = phoneNumberUtil.getAsYouTypeFormatter(updatedLocale.country)
        i18nMatcher = String.format("+%s", templateNumber.countryCode)
        ccMatcher = String.format("%s", templateNumber.countryCode)
        formattedTemplateNumber = phoneNumberUtil.format(templateNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (selfChanged) {
            return
        }

        stopWatching = containsNonDialableDigit(s.toString().toCharArray())
    }

    @Synchronized
    override fun afterTextChanged(s: Editable) {
        if (selfChanged || stopWatching) {
            return
        }

        val text = formatInput(s)
        selfChanged = true
        s.replace(0, s.length, text, 0, text.length)
        // another watcher can compete with our change
        if (text == s.toString()) {
            Selection.setSelection(s, text.length)
        }
        selfChanged = false
        validateNumber(text)
    }

    private fun validateNumber(text: String) {
        if (stripSeparators(text).length < MIN_PHONE_NUMBER_LENGTH) {
            return
        }

        try {
            phoneNumber = phoneNumberUtil.parse(text, locale.country)
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                callback.onPhoneNumberValid(phoneNumber)
            } else if (formattedTemplateNumber.length <= text.length) {
                callback.onPhoneNumberInValid(text)
            }
        } catch (e: NumberParseException) {
            e.printStackTrace()
            callback.onPhoneNumberInValid(text)
        }

    }

    private fun prependCountryCode(text: String): String {
        return if (text.startsWith(i18nMatcher)) {
            text
        } else if (text.startsWith(ccMatcher) && text.length >= ccMatcher.length) {
            prependCountryCode(text.replaceFirst(ccMatcher.toRegex(), ""))
        } else if (!text.startsWith(i18nMatcher) && text.length >= i18nMatcher.length) {
            String.format("%s%s", i18nMatcher, text)
        } else if (!text.startsWith("+") && !text.startsWith(ccMatcher.substring(0, 1))) {
            String.format("%s%s", i18nMatcher, text)
        } else {
            i18nMatcher
        }
    }

    private fun containsNonDialableDigit(chars: CharArray): Boolean {
        for (c in chars) {
            if (!PhoneNumberUtils.isNonSeparator(c) && !discardableValues.containsKey(c.toString())) {
                return true
            }
        }
        return false
    }

    private fun formatInput(s: Editable): String {
        return formatInput(s.toString())
    }

    private fun formatInput(s: String): String {
        var text = ""
        var current = prependCountryCode(s)
        formatter.clear()
        current = stripSeparators(current)

        for (c in current.toCharArray()) {
            text = formatter.inputDigit(c)
        }

        return text
    }

    private fun stripSeparators(text: String): String {
        var copy = PhoneNumberUtils.stripSeparators(text)
        for (key in discardableValues.keys) {
            copy = copy.replace(key, discardableValues[key] ?: key)
        }
        return copy
    }


    interface Callback {
        fun onPhoneNumberValid(phoneNumber: Phonenumber.PhoneNumber?)

        fun onPhoneNumberInValid(text: String)
    }

    companion object {

        internal const val MIN_PHONE_NUMBER_LENGTH = 6
    }
}
