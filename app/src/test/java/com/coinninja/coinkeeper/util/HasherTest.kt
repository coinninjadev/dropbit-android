package com.coinninja.coinkeeper.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.model.PhoneNumber
import com.google.i18n.phonenumbers.Phonenumber
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(AndroidJUnit4::class)
class HasherTest {

    @Test
    fun hashes_phone_number() {
        val phoneNumber = "13305551111"
        val hash = "710c3ec37d3bbab4d9b140656ea8ab28d14bad091e12b912dc73d0fbcd78664d"
        assertThat(Hasher().hash(phoneNumber), equalTo(hash))
    }

    @Test
    fun hashes_phone_number_obj() {
        val phoneNumber = Phonenumber.PhoneNumber()
        phoneNumber.countryCode = 54
        phoneNumber.nationalNumber = 3512522169L

        var pn = PhoneNumber(phoneNumber)
        val hasher = Hasher()
        assertThat(hasher.hash(pn), equalTo("5cd083b3655f5fda08a57b2a5a6ba67ea1054a0efd4f641337620bde05787b14"))

        phoneNumber.nationalNumber = 93512522169L
        pn = PhoneNumber(phoneNumber)
        assertThat(hasher.hash(pn), equalTo("5cd083b3655f5fda08a57b2a5a6ba67ea1054a0efd4f641337620bde05787b14"))
    }
}