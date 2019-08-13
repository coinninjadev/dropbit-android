package com.coinninja.coinkeeper.text

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.os.ConfigurationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.matchers.TextViewMatcher.hasText
import com.google.i18n.phonenumbers.Phonenumber
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
class PhoneNumberFormattingTextWatcherTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private var callback: PhoneNumberFormattingTextWatcher.Callback = mock()
    private var watcher: PhoneNumberFormattingTextWatcher = PhoneNumberFormattingTextWatcher(
            ConfigurationCompat.getLocales(context.resources.configuration).get(0),
            callback)
    private var editText: EditText = EditText(context).also {
        it.addTextChangedListener(watcher)
    }

    @Test
    fun handles_paste_of_number() {
        val expected = 3304175555L
        val phoneNumber = Phonenumber.PhoneNumber()
        phoneNumber.nationalNumber = expected
        phoneNumber.countryCode = 1

        editText.setText("+13304175555")
        assertThat(editText.text.toString(), equalTo("+1 330-417-5555"))

        editText.setText("13304175555")
        assertThat(editText.text.toString(), equalTo("+1 330-417-5555"))

        verify(callback, atLeast(1)).onPhoneNumberValid(phoneNumber)
    }

    @Test
    fun handles_paste_of_number_parens() {
        val expected = 3304175555L
        val phoneNumber = Phonenumber.PhoneNumber()
        phoneNumber.nationalNumber = expected
        phoneNumber.countryCode = 1
        editText.setText("(330) 417-5555")
        assertThat(editText.text.toString(), equalTo("+1 330-417-5555"))

        verify(callback).onPhoneNumberValid(phoneNumber)
    }

    @Test
    fun formats_after_region_code_change() {
        editText.setText("330")
        editText.setText("3304")
        assertThat(editText.text.toString(), equalTo("+1 330-4"))
    }

    @Test
    fun formats_subscription_values() {
        editText.setText("330 41788")
        assertThat(editText.text.toString(), equalTo("+1 330-417-88"))
        editText.setText("330-417-8888")
        assertThat(editText.text.toString(), equalTo("+1 330-417-8888"))
    }

    @Test
    fun digit_incremental() {
        editText.setText("3")
        assertThat(editText.text.toString(), equalTo("+1 3"))
        editText.setText("33")
        assertThat(editText.text.toString(), equalTo("+1 33"))
        editText.setText("330")
        assertThat(editText.text.toString(), equalTo("+1 330"))
        editText.setText("330-")
        assertThat(editText.text.toString(), equalTo("+1 330"))
        editText.setText("3304")
        assertThat(editText.text.toString(), equalTo("+1 330-4"))
        editText.setText("33041")
        assertThat(editText.text.toString(), equalTo("+1 330-41"))
        editText.setText("330417")
        assertThat(editText.text.toString(), equalTo("+1 330-417"))
        editText.setText("330-417-")
        assertThat(editText.text.toString(), equalTo("+1 330-417"))
        editText.setText("3304178")
        assertThat(editText.text.toString(), equalTo("+1 330-417-8"))
        editText.setText("33041788")
        assertThat(editText.text.toString(), equalTo("+1 330-417-88"))
        editText.setText("330417888")
        assertThat(editText.text.toString(), equalTo("+1 330-417-888"))
        editText.setText("3304178888")
        assertThat(editText.text.toString(), equalTo("+1 330-417-8888"))
        editText.setText("33041788888")
        assertThat(editText.text.toString(), equalTo("+133041788888"))
    }

    @Test
    fun formats_autofilled_textviews() {
        val expected = 3304178888L
        val phoneNumber = Phonenumber.PhoneNumber()
        phoneNumber.nationalNumber = expected
        phoneNumber.countryCode = 1

        editText.setText("13304178888")

        verify(callback).onPhoneNumberValid(phoneNumber)
    }

    @Test
    fun lets_callback_know_when_a_valid_phone_number_found() {
        val expected = 3304178888L
        val phoneNumber = Phonenumber.PhoneNumber()
        phoneNumber.nationalNumber = expected
        phoneNumber.countryCode = 1

        editText.setText("(330) 417-8888")

        verify(callback).onPhoneNumberValid(phoneNumber)
    }

    @Test
    fun lets_callback_know_that_number_is_invalid() {
        val text = "3333333333"
        editText.setText(text)

        verify(callback).onPhoneNumberInValid("+1 333-333-3333")
    }

    @Test
    fun characters_pause_formatting() {
        var text = "3304178c"
        editText.setText(text)
        assertThat(editText.text.toString(), equalTo(text))

        text = "33041788"
        editText.setText(text)
        assertThat(editText.text.toString(), equalTo("+1 330-417-88"))
    }

    @Test
    fun updates_locale_and_formatting_rules__US_to_GB() {
        watcher.updateLocale(Locale("en", "GB"))

        editText.setText("+441632960025")

        assertThat(editText, hasText("+44 1632 960025"))
    }

    @Test
    fun updates_locale_and_formatting_rules__US_to_JP() {
        watcher.updateLocale(Locale("ja", "JP"))

        editText.setText("+819068427999")

        assertThat(editText, hasText("+81 90-6842-7999"))
    }

    @Test
    fun prepends_country_code() {
        watcher.updateLocale(Locale("ja", "JP"))

        editText.setText("9068427999")
        assertThat(editText, hasText("+81 90-6842-7999"))

        editText.setText("+819068427999")
        assertThat(editText, hasText("+81 90-6842-7999"))

        editText.setText("819068427999")
        assertThat(editText, hasText("+81 90-6842-7999"))

        editText.setText("9")
        assertThat(editText, hasText("+81 9"))

        editText.setText("+819")
        assertThat(editText, hasText("+81 9"))

        editText.setText("+8")
        assertThat(editText, hasText("+81 "))

        editText.setText("")
        assertThat(editText, hasText("+81 "))
        verify(callback, times(0)).onPhoneNumberInValid(any())
    }

    @Test
    @Config(qualifiers = "en-rNZ")
    fun validates_multiple_length_countries__New_Zealand() {
        val number = Phonenumber.PhoneNumber()
        number.countryCode = 64
        number.nationalNumber = 21345687L
        editText.setText("21345687")
        assertThat(editText, hasText("+64 21 345 687"))
        verify(callback).onPhoneNumberValid(number)
    }

    @Test
    @Config(qualifiers = "zh-rTW")
    fun validates_multiple_length_countries_leading_0__Tw() {
        val number = Phonenumber.PhoneNumber()
        number.countryCode = 886
        number.nationalNumber = 912345678
        editText.setText("0")
        assertThat(editText, hasText("+886 0"))
        editText.setText("09")
        assertThat(editText, hasText("+886 09"))
        editText.setText("091")
        assertThat(editText, hasText("+886091"))
        editText.setText("0912")
        assertThat(editText, hasText("+8860912"))
        editText.setText("09123")
        assertThat(editText, hasText("+88609123"))
        editText.setText("091234")
        assertThat(editText, hasText("+886091234"))
        editText.setText("0912345")
        assertThat(editText, hasText("+8860912345"))
        editText.setText("09123456")
        assertThat(editText, hasText("+88609123456"))
        editText.setText("091234567")
        assertThat(editText, hasText("+886091234567"))
        editText.setText("0912345678")
        assertThat(editText, hasText("+8860912345678"))

        verify(callback).onPhoneNumberValid(number)
        verify(callback, times(0)).onPhoneNumberInValid(any())
    }

    @Test
    @Config(qualifiers = "zh-rTW")
    fun validates_multiple_length_countries__Tw() {
        val number = Phonenumber.PhoneNumber()
        number.countryCode = 886
        number.nationalNumber = 912345678
        editText.setText("9")
        assertThat(editText, hasText("+886 9"))
        editText.setText("91")
        assertThat(editText, hasText("+886 91"))
        editText.setText("912")
        assertThat(editText, hasText("+886 912"))
        editText.setText("9123")
        assertThat(editText, hasText("+886 912 3"))
        editText.setText("91234")
        assertThat(editText, hasText("+886 912 34"))
        editText.setText("912345")
        assertThat(editText, hasText("+886 912 345"))
        editText.setText("9123456")
        assertThat(editText, hasText("+886 912 345 6"))
        editText.setText("91234567")
        assertThat(editText, hasText("+886 912 345 67"))
        editText.setText("912345678")
        assertThat(editText, hasText("+886 912 345 678"))

        verify(callback).onPhoneNumberValid(number)
        verify(callback, times(0)).onPhoneNumberInValid(any())
    }

    @Test
    @Config(qualifiers = "he-rIL")
    fun validates_multiple_length_countries__IL() {
        val number = Phonenumber.PhoneNumber()
        number.countryCode = 972
        number.nationalNumber = 501234567
        editText.setText("5")
        assertThat(editText, hasText("+972 5"))
        editText.setText("50")
        assertThat(editText, hasText("+972 50"))
        editText.setText("501")
        assertThat(editText, hasText("+972 50-1"))
        editText.setText("5012")
        assertThat(editText, hasText("+972 50-12"))
        editText.setText("50123")
        assertThat(editText, hasText("+972 50-123"))
        editText.setText("501234")
        assertThat(editText, hasText("+972 50-123-4"))
        editText.setText("5012345")
        assertThat(editText, hasText("+972 50-123-45"))
        editText.setText("50123456")
        assertThat(editText, hasText("+972 50-123-456"))
        editText.setText("501234567")
        assertThat(editText, hasText("+972 50-123-4567"))

        verify(callback).onPhoneNumberValid(number)
        verify(callback, times(0)).onPhoneNumberInValid(any())
    }

    @Test
    @Config(qualifiers = "de-rDE")
    fun validates_multiple_length_countries__DE() {
        val number = Phonenumber.PhoneNumber()
        number.countryCode = 49
        number.nationalNumber = 15123456789
        editText.setText("1")
        assertThat(editText, hasText("+49 1"))
        editText.setText("15")
        assertThat(editText, hasText("+49 15"))
        editText.setText("151")
        assertThat(editText, hasText("+49 151"))
        editText.setText("1512")
        assertThat(editText, hasText("+49 1512"))
        editText.setText("15123")
        assertThat(editText, hasText("+49 1512 3"))
        editText.setText("151234")
        assertThat(editText, hasText("+49 1512 34"))
        editText.setText("1512345")
        assertThat(editText, hasText("+49 1512 345"))
        editText.setText("15123456")
        assertThat(editText, hasText("+49 1512 3456"))
        editText.setText("151234567")
        assertThat(editText, hasText("+49 1512 34567"))
        editText.setText("1512345678")
        assertThat(editText, hasText("+49 1512 345678"))
        editText.setText("15123456789")
        assertThat(editText, hasText("+49 1512 3456789"))

        verify(callback).onPhoneNumberValid(number)
        verify(callback, times(0)).onPhoneNumberInValid(any())
    }


    @Test
    fun plays_nice_with_other_watchers___passive___we_aint_got_time_for_that() {
        val competitiveWatcher = CompetitiveWatcher()

        editText.addTextChangedListener(competitiveWatcher)
        editText.setText("3304")

        assertThat(editText.text.toString(), equalTo(""))
    }

    private inner class CompetitiveWatcher : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (!s.toString().isEmpty()) {
                s.replace(0, s.length, "")
            }
        }
    }
}