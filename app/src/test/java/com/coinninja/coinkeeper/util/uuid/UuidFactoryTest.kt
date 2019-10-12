package com.coinninja.coinkeeper.util.uuid

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class UuidFactoryTest {

    @Test
    fun provides_uuid_from_preferences_when_available() {
        val uuidFactory = UuidFactory(mock(), mock())
        val key = UuidFactory.PREFERENCES_UUID
        val defaultValue = ""
        val savedUUID = "---- Saved UUID -----"
        whenever(uuidFactory.preferencesUtil.getString(key, defaultValue)).thenReturn(savedUUID)
        val providedUUID = uuidFactory.provideUuid()
        verify(uuidFactory.uuidGenerator, times(0)).generate()
        assertThat(providedUUID, equalTo(savedUUID))
    }

    @Test
    fun generates_uuid() {
        val sampleUUID = "--- Generated UUID ---"
        val uuidFactory = UuidFactory(mock(), mock())
        whenever(uuidFactory.preferencesUtil.getString(UuidFactory.PREFERENCES_UUID, "")).thenReturn("")
        whenever(uuidFactory.uuidGenerator.generate()).thenReturn(sampleUUID)

        val providedUUID = uuidFactory.provideUuid()

        assertThat(providedUUID, equalTo(sampleUUID))
        verify(uuidFactory.preferencesUtil).savePreference(UuidFactory.PREFERENCES_UUID, sampleUUID)
    }
}