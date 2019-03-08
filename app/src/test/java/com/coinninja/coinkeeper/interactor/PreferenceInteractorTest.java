package com.coinninja.coinkeeper.interactor;

import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PreferenceInteractorTest {

    @Mock
    PreferencesUtil preferencesUtil;

    private PreferenceInteractor preferenceInteractor;

    @Mock
    PreferencesUtil.Callback preferenceWriteCallback;

    @Before
    public void setUp() {
        preferenceInteractor = new PreferenceInteractor(preferencesUtil);
    }

    @Test
    public void fetches_permission_to_show_invite_help() {
        when(preferencesUtil.getBoolean(PreferenceInteractor.PREFERENCE_SKIP_INVITE_HELP)).
                thenReturn(true);

        boolean shouldShowInviteHelp = preferenceInteractor.getShouldShowInviteHelp();

        verify(preferencesUtil).getBoolean(PreferenceInteractor.PREFERENCE_SKIP_INVITE_HELP);
        assertFalse(shouldShowInviteHelp);
    }

    @Test
    public void allows_for_skipping_of_invite_help_screen() {
        preferenceInteractor.skipInviteHelpScreen(preferenceWriteCallback);

        verify(preferencesUtil).savePreference(eq(PreferenceInteractor.PREFERENCE_SKIP_INVITE_HELP),
                eq(true), eq(preferenceWriteCallback));
    }

}