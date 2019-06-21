package com.coinninja.coinkeeper.util.android

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.AsyncTask
import app.dropbit.annotations.Mockable
import javax.inject.Inject

@Mockable
class PreferencesUtil @SuppressLint("CommitPrefEdits")
@Inject
constructor(internal val preferences: SharedPreferences) {
    internal var preferenceWriter: PreferenceWriter? = null
    internal val editor: SharedPreferences.Editor

    init {
        editor = preferences.edit()
        preferenceWriter = PreferenceWriter()
    }

    fun savePreference(key: String, value: Long) {
        editor.putLong(key, value)
        editor.apply()
    }

    fun savePreference(key: String, value: Boolean, callback: Callback) {
        preferenceWriter?.clone()?.setPreference(editor, callback, key, value)?.execute()
    }

    fun savePreference(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun savePreference(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun removePreference(key: String) {
        editor.remove(key)
        editor.apply()
    }

    fun getLong(key: String, value: Long): Long {
        return preferences.getLong(key, value)
    }

    @JvmOverloads
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String? {
        return preferences.getString(key, defaultValue)
    }

    operator fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    fun removeAll() {
        editor.clear()
        editor.apply()
    }

    interface Callback {
        fun onComplete()
    }

    @Mockable
    internal class PreferenceWriter : AsyncTask<Void?, Void?, Void?>() {

        var callback: Callback? = null
        var editor: SharedPreferences.Editor? = null

        public override fun doInBackground(vararg voids: Void?): Void? {
            editor!!.apply()
            return null
        }

        fun clone(): PreferenceWriter {
            return PreferenceWriter()
        }

        public override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            callback?.onComplete()
        }

        fun setPreference(editor: SharedPreferences.Editor?, callback: Callback, key: String, value: Boolean): PreferenceWriter {
            this.editor = editor
            this.callback = callback
            this.editor!!.putBoolean(key, value)
            return this
        }
    }
}
