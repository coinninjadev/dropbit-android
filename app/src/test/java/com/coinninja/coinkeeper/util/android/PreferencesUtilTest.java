package com.coinninja.coinkeeper.util.android;

import android.content.SharedPreferences;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.interactor.PreferenceInteractor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class PreferencesUtilTest {

    @Mock
    SharedPreferences sharedPreferences;

    @Mock
    SharedPreferences.Editor editor;

    @Mock
    PreferencesUtil.Callback callback;

    private PreferencesUtil preferencesUtil;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(sharedPreferences.edit()).thenReturn(editor);

        preferencesUtil = new PreferencesUtil(sharedPreferences);
    }

    @Test
    public void allows_write_of_string_preference() {
        String value = "value";
        String key = "key";

        preferencesUtil.savePreference(key, value);

        verify(editor).putString(key, value);
        verify(editor).apply();
    }

    @Test
    public void allows_write_of_boolean_preference_with_callback() {
        String key = "key";
        boolean value = false;
        PreferencesUtil.PreferenceWriter preferenceWriter = mock(PreferencesUtil.PreferenceWriter.class);
        preferencesUtil.preferenceWriter = preferenceWriter;
        when(preferenceWriter.clone()).thenReturn(preferenceWriter);
        when(preferenceWriter.setPreference(editor, callback, key, value)).thenReturn(preferenceWriter);

        preferencesUtil.savePreference(key, value, callback);

        verify(preferenceWriter).clone();
        verify(preferenceWriter).setPreference(editor, callback, key, value);
        verify(preferenceWriter).execute();
    }

    @Test
    public void preference_util_saves_long_value_to_shared_pref_test() {
        long sampleLongValue = 5546546l;
        String sampleKey = "SOME - KEY";

        preferencesUtil.savePreference(sampleKey, sampleLongValue);

        verify(editor).putLong(sampleKey, sampleLongValue);
        verify(editor).apply();
    }

    @Test
    public void preference_util_retrieves_long_value_to_shared_pref_test() {
        long sampleLongValue = 5546546l;
        long sampleDefaultValue = 0l;
        String sampleKey = "SOME - KEY";
        when(sharedPreferences.getLong(sampleKey, sampleDefaultValue)).thenReturn(sampleLongValue);

        long savedValue = preferencesUtil.getLong(sampleKey, sampleDefaultValue);

        assertThat(savedValue, equalTo(sampleLongValue));
    }

    @Test
    public void can_save_boolean_to_shared_prefereneces() {
        preferencesUtil.savePreference(PreferenceInteractor.PREFERENCE_FINGERPRINT, true);

        verify(editor).putBoolean(PreferenceInteractor.PREFERENCE_FINGERPRINT, true);
        verify(editor).apply();
    }

    @Test
    public void removes_shared_preference_by_key_value() {
        String key = "---- some key";

        preferencesUtil.removePreference(key);

        verify(editor).remove(key);
        verify(editor).apply();
    }

    @Test
    public void can_retrieve_boolean_from_preferences() {
        when(sharedPreferences.getBoolean(PreferenceInteractor.PREFERENCE_FINGERPRINT, false)).thenReturn(true);

        assertTrue(preferencesUtil.getBoolean(PreferenceInteractor.PREFERENCE_FINGERPRINT));

    }

    @Test
    public void commits_preference_asyncronoslly() {
        PreferencesUtil.PreferenceWriter preferenceWriter = new PreferencesUtil.PreferenceWriter();
        preferenceWriter.editor = editor;
        preferenceWriter.callback = callback;

        preferenceWriter.doInBackground();

        verify(editor).apply();
    }

    @Test
    public void notifies_caller_that_write_complete() {
        PreferencesUtil.PreferenceWriter preferenceWriter = new PreferencesUtil.PreferenceWriter();
        preferenceWriter.editor = editor;
        preferenceWriter.callback = callback;

        preferenceWriter.onPostExecute(null);

        verify(callback).onComplete();
    }

    @Test
    public void allows_task_to_be_cloned() {
        PreferencesUtil.PreferenceWriter prefererenceWriter = new PreferencesUtil.PreferenceWriter();

        assertTrue(prefererenceWriter.clone() instanceof PreferencesUtil.PreferenceWriter);
    }


    @Test
    public void preference_writer_can_write_booleans() {
        String key = "key";
        boolean value = false;

        PreferencesUtil.PreferenceWriter preferenceWriter = new PreferencesUtil.PreferenceWriter();
        preferenceWriter.setPreference(editor, callback, key, value);

        verify(editor).putBoolean(key, value);
        assertThat(preferenceWriter.editor, equalTo(editor));
        assertThat(preferenceWriter.callback, equalTo(callback));

    }

    @Test
    public void fetch_string_from_local_storage() {
        when(sharedPreferences.getString("FOO", "")).thenReturn("bar");

        assertThat(preferencesUtil.getString("FOO", ""), equalTo("bar"));
    }

    @Test
    public void wraps_knowlege_of_presence_of_key() {
        String key = "foo";
        preferencesUtil.contains(key);
        verify(sharedPreferences).contains(key);
    }
}