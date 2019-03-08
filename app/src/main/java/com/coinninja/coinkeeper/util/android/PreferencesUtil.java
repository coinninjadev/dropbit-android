package com.coinninja.coinkeeper.util.android;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import javax.inject.Inject;

public class PreferencesUtil {

    private final SharedPreferences preferences;
    PreferenceWriter preferenceWriter;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    @Inject
    public PreferencesUtil(SharedPreferences preferences) {
        this.preferences = preferences;
        editor = preferences.edit();
        preferenceWriter = new PreferenceWriter();
    }

    public void savePreference(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    public void savePreference(String key, boolean value, Callback callback) {
        preferenceWriter.clone()
                .setPreference(editor, callback, key, value)
                .execute();
    }

    public void savePreference(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void savePreference(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public void removePreference(String key) {
        editor.remove(key);
        editor.apply();
    }

    public long getLong(String key, long value) {
        return preferences.getLong(key, value);
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public boolean contains(String key) {
        return preferences.contains(key);
    }

    public interface Callback {
        void onComplete();
    }

    static class PreferenceWriter extends AsyncTask<Void, Void, Void> {

        Callback callback;
        SharedPreferences.Editor editor;

        @Override
        protected Void doInBackground(Void... voids) {
            editor.apply();
            return null;
        }

        @Override
        public PreferenceWriter clone() {
            return new PreferenceWriter();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            callback.onComplete();
        }

        public PreferenceWriter setPreference(SharedPreferences.Editor editor, Callback callback, String key, boolean value) {
            this.editor = editor;
            this.callback = callback;
            this.editor.putBoolean(key, value);
            return this;
        }
    }
}
