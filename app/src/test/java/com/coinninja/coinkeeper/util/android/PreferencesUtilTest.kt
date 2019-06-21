package com.coinninja.coinkeeper.util.android

import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.interactor.UserPreferences
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
class PreferencesUtilTest {

    fun setUp(): PreferencesUtil {
        val preferences = mock(SharedPreferences::class.java)
        whenever(preferences.edit()).thenReturn(mock(SharedPreferences.Editor::class.java))
        return PreferencesUtil(preferences)
    }

    @Test
    fun `allows write of string preference`() {
        val util = setUp()
        val value = "value"
        val key = "key"

        util.savePreference(key, value)

        verify(util.editor).putString(key, value)
        verify(util.editor).apply()
    }

    @Test
    fun `allows write of boolean preference with callback`() {
        val util = setUp()
        val key = "key"
        val value = false
        val preferenceWriter = mock(PreferencesUtil.PreferenceWriter::class.java)
        val callback = mock(PreferencesUtil.Callback::class.java)
        util.preferenceWriter = preferenceWriter
        whenever(preferenceWriter.clone()).thenReturn(preferenceWriter)
        whenever(preferenceWriter.setPreference(util.editor, callback, key, value)).thenReturn(preferenceWriter)

        util.savePreference(key, value, callback)

        verify(preferenceWriter).clone()
        verify(preferenceWriter).setPreference(util.editor, callback, key, value)
        verify(preferenceWriter).execute()
    }

    @Test
    fun `preference util saves long value to shared pref test`() {
        val util = setUp()
        val sampleLongValue = 5546546L
        val sampleKey = "SOME - KEY"

        util.savePreference(sampleKey, sampleLongValue)

        verify(util.editor).putLong(sampleKey, sampleLongValue)
        verify(util.editor).apply()
    }

    @Test
    fun `preference util retrieves long value to shared pref test`() {
        val util = setUp()
        val sampleLongValue = 5546546L
        val sampleDefaultValue = 0L
        val sampleKey = "SOME - KEY"
        whenever(util.preferences.getLong(sampleKey, sampleDefaultValue)).thenReturn(sampleLongValue)

        val savedValue = util.getLong(sampleKey, sampleDefaultValue)

        assertThat(savedValue, equalTo(sampleLongValue))
    }

    @Test
    fun `can save boolean to shared prefereneces`() {
        val util = setUp()
        util.savePreference(UserPreferences.PREFERENCE_FINGERPRINT, true)

        verify(util.editor).putBoolean(UserPreferences.PREFERENCE_FINGERPRINT, true)
        verify(util.editor).apply()
    }

    @Test
    fun `removes shared preference by key value`() {
        val util = setUp()
        val key = "---- some key"

        util.removePreference(key)

        verify(util.editor).remove(key)
        verify(util.editor).apply()
    }

    @Test
    fun `can retrieve boolean from preferences`() {
        val util = setUp()
        whenever(util.preferences.getBoolean(UserPreferences.PREFERENCE_FINGERPRINT, false)).thenReturn(true)

        assertTrue(util.getBoolean(UserPreferences.PREFERENCE_FINGERPRINT))

    }

    @Test
    fun `commits preference asyncronoslly`() {
        val util = setUp()
        val preferenceWriter = PreferencesUtil.PreferenceWriter()
        val callback = mock(PreferencesUtil.Callback::class.java)
        preferenceWriter.editor = util.editor
        preferenceWriter.callback = callback

        preferenceWriter.doInBackground()

        verify(util.editor).apply()
    }

    @Test
    fun `notifies caller that write complete`() {
        val util = setUp()
        val preferenceWriter = PreferencesUtil.PreferenceWriter()
        val callback = mock(PreferencesUtil.Callback::class.java)
        preferenceWriter.editor = util.editor
        preferenceWriter.callback = callback

        preferenceWriter.onPostExecute(null)

        verify<PreferencesUtil.Callback>(callback).onComplete()
    }

    @Test
    fun `allows task to be cloned`() {
        val preferenceWriter = PreferencesUtil.PreferenceWriter()

        assertTrue(preferenceWriter.clone() is PreferencesUtil.PreferenceWriter)
    }

    @Test
    fun `preference writer can write booleans`() {
        val util = setUp()
        val key = "key"
        val value = false
        val callback = mock(PreferencesUtil.Callback::class.java)

        val preferenceWriter = PreferencesUtil.PreferenceWriter()
        preferenceWriter.setPreference(util.editor, callback, key, value)

        verify(util.editor).putBoolean(key, value)
        assertThat(preferenceWriter.editor, equalTo(util.editor))
        assertThat(preferenceWriter.callback, equalTo(callback))

    }

    @Test
    fun `fetch string from local storage`() {
        val util = setUp()
        whenever(util.preferences.getString("FOO", "")).thenReturn("bar")

        assertThat(util.getString("FOO", ""), equalTo("bar"))
    }

    @Test
    fun `wraps knowledge of presence of key`() {
        val util = setUp()
        val key = "foo"
        util.contains(key)
        verify(util.preferences).contains(key)
    }

    @Test
    fun `clears preferences`() {
        val util = setUp()

        util.removeAll()

        verify(util.editor).clear()
        verify(util.editor).apply()
    }
}