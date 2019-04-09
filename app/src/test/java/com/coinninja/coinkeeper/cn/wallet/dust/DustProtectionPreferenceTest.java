package com.coinninja.coinkeeper.cn.wallet.dust;

import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DustProtectionPreferenceTest {

    @Mock
    PreferencesUtil preferencesUtil;

    @InjectMocks
    DustProtectionPreference dustProtectionPreference;

    @Before
    public void setUp() {
        when(preferencesUtil.contains(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(false);
    }

    @After
    public void tearDown() {
        preferencesUtil = null;
        dustProtectionPreference = null;
    }

    @Test
    public void dust_protection_disabled_by_default() {
        assertFalse(dustProtectionPreference.isDustProtectionEnabled());
    }

    @Test
    public void can_toggle_dust_protection_preference__given_no_stored_preference() {
        when(preferencesUtil.contains(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(false).thenReturn(true);
        when(preferencesUtil.getBoolean(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(true);

        assertTrue(dustProtectionPreference.toggleDustProtection());
        verify(preferencesUtil).savePreference(DustProtectionPreference.PREFERENCE_KEY, true);
        verify(preferencesUtil).getBoolean(DustProtectionPreference.PREFERENCE_KEY);
        verify(preferencesUtil, times(2)).contains(DustProtectionPreference.PREFERENCE_KEY);
        verifyNoMoreInteractions(preferencesUtil);

    }

    @Test
    public void can_toggle_dust_protection_preference__given_stored_preference__false() {
        when(preferencesUtil.contains(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(true);
        when(preferencesUtil.getBoolean(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(false).thenReturn(true);

        assertTrue(dustProtectionPreference.toggleDustProtection());
        verify(preferencesUtil).savePreference(DustProtectionPreference.PREFERENCE_KEY, true);
        verify(preferencesUtil, times(2)).getBoolean(DustProtectionPreference.PREFERENCE_KEY);
        verify(preferencesUtil, times(2)).contains(DustProtectionPreference.PREFERENCE_KEY);
        verifyNoMoreInteractions(preferencesUtil);

    }

    @Test
    public void can_toggle_dust_protection_preference__given_stored_preference__true() {
        when(preferencesUtil.contains(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(true);
        when(preferencesUtil.getBoolean(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(true).thenReturn(false);

        assertFalse(dustProtectionPreference.toggleDustProtection());
        verify(preferencesUtil).savePreference(DustProtectionPreference.PREFERENCE_KEY, false);
        verify(preferencesUtil, times(2)).getBoolean(DustProtectionPreference.PREFERENCE_KEY);
        verify(preferencesUtil, times(2)).contains(DustProtectionPreference.PREFERENCE_KEY);
        verifyNoMoreInteractions(preferencesUtil);

    }

    @Test
    public void set_protection__turn_off() {
        when(preferencesUtil.contains(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(true);
        when(preferencesUtil.getBoolean(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(true).thenReturn(false);

        dustProtectionPreference.setProtection(false);
        verify(preferencesUtil).savePreference(DustProtectionPreference.PREFERENCE_KEY, false);
    }

    @Test
    public void set_protection__turn_on() {
        when(preferencesUtil.contains(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(true);
        when(preferencesUtil.getBoolean(DustProtectionPreference.PREFERENCE_KEY)).thenReturn(true).thenReturn(false);

        dustProtectionPreference.setProtection(true);
        verify(preferencesUtil).savePreference(DustProtectionPreference.PREFERENCE_KEY, true);
    }
}