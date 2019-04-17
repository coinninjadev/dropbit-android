package com.coinninja.coinkeeper.util.uuid;

import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UuidFactoryTest {

    @Mock
    private PreferencesUtil preferencesUtil;

    @Mock
    private UUIDGenerator uuidGenerator;

    @InjectMocks
    private UuidFactory uuidFactory;

    @Before
    public void setUp() {
        when(preferencesUtil.getString(UuidFactory.PREFERENCES_UUID, "")).thenReturn("");
    }

    @After
    public void tearDown() {
        preferencesUtil = null;
        uuidGenerator = null;
        uuidFactory = null;
    }

    @Test
    public void provides_uuid_from_preferences_when_available() {
        String key = UuidFactory.PREFERENCES_UUID;
        String defaultValue = "";
        String savedUUID = "---- Saved UUID -----";
        when(preferencesUtil.getString(key, defaultValue)).thenReturn(savedUUID);

        String providedUUID = uuidFactory.provideUuid();

        verify(uuidGenerator, times(0)).generate();
        assertThat(providedUUID, equalTo(savedUUID));
    }

    @Test
    public void generates_uuid() {
        String sampleUUID = "--- Generated UUID ---";
        when(uuidGenerator.generate()).thenReturn(sampleUUID);

        String providedUUID = uuidFactory.provideUuid();

        assertThat(providedUUID, equalTo(sampleUUID));
        verify(preferencesUtil).savePreference(UuidFactory.PREFERENCES_UUID, sampleUUID);
    }
}