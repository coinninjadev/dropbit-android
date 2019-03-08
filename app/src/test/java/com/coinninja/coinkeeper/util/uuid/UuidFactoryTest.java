package com.coinninja.coinkeeper.util.uuid;

import com.coinninja.coinkeeper.model.db.User;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UuidFactoryTest {

    @Mock
    UserHelper userHelper;

    @Mock
    PreferencesUtil preferencesUtil;

    @Mock
    UUIDGenerator uuidGenerator;

    @InjectMocks
    UuidFactory uuidFactory;


    @Before
    public void setUp() {
        when(preferencesUtil.getString(UuidFactory.PREFERENCES_UUID, "")).thenReturn("");
    }


    @Test
    public void provides_uuid_from_preferences_when_available() {
        String key = UuidFactory.PREFERENCES_UUID;
        String defaultValue = "";
        String savedUUID = "---- Saved UUID -----";
        when(preferencesUtil.getString(key, defaultValue)).thenReturn(savedUUID);

        String providedUUID = uuidFactory.provideUuid();

        verify(uuidGenerator, times(0)).generate();
        verify(userHelper, times(0)).getUniqueID();
        assertThat(providedUUID, equalTo(savedUUID));
    }

    @Test
    public void generates_uuid() {
        String sampleUUID = "--- Generated UUID ---";
        when(uuidGenerator.generate()).thenReturn(sampleUUID);

        String providedUUID = uuidFactory.provideUuid();

        assertThat(providedUUID, equalTo(sampleUUID));
    }

    @Test
    public void saves_uuid_local_upon_generation() {
        String uuid = "--- Generated UUID --";
        when(uuidGenerator.generate()).thenReturn(uuid);

        uuidFactory.provideUuid();

        verify(preferencesUtil).savePreference(UuidFactory.PREFERENCES_UUID, uuid);
    }

    @Test
    public void saves_from_user_when_unavailable_locally_and_available_on_the_user() {
        String uuid = "-- db uuid";
        when(userHelper.getUser()).thenReturn(mock(User.class));
        when(userHelper.getUniqueID()).thenReturn(uuid);

        uuidFactory.provideUuid();

        verify(preferencesUtil).savePreference(UuidFactory.PREFERENCES_UUID, uuid);
        verify(uuidGenerator, times(0)).generate();
    }

}