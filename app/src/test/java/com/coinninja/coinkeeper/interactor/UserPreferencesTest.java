package com.coinninja.coinkeeper.interactor;

import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserPreferencesTest {

    @Mock
    private PreferencesUtil preferencesUtil;

    @InjectMocks
    private UserPreferences userPreferences;

    @Mock
    private PreferencesUtil.Callback preferenceWriteCallback;


    @Test
    public void fetches_permission_to_show_invite_help() {
        when(preferencesUtil.getBoolean(UserPreferences.PREFERENCE_SKIP_INVITE_HELP)).
                thenReturn(true);

        boolean shouldShowInviteHelp = userPreferences.getShouldShowInviteHelp();

        verify(preferencesUtil).getBoolean(UserPreferences.PREFERENCE_SKIP_INVITE_HELP);
        assertFalse(shouldShowInviteHelp);
    }

    @Test
    public void allows_for_skipping_of_invite_help_screen() {
        userPreferences.skipInviteHelpScreen(preferenceWriteCallback);

        verify(preferencesUtil).savePreference(eq(UserPreferences.PREFERENCE_SKIP_INVITE_HELP),
                eq(true), eq(preferenceWriteCallback));
    }

}