package com.coinninja.coinkeeper.util.uuid

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import javax.inject.Inject

@Mockable
class UuidFactory @Inject internal constructor(
        internal val preferencesUtil: PreferencesUtil,
        internal val uuidGenerator: UUIDGenerator
) {
    fun provideUuid(): String {
        var uuid = preferencesUtil.getString(PREFERENCES_UUID, "")
        if (uuid.isEmpty()) {
            uuid = uuidGenerator.generate()
            preferencesUtil.savePreference(PREFERENCES_UUID, uuid)
        }
        return uuid
    }

    companion object {
        internal const val PREFERENCES_UUID = "PREFERENCES_UUID"
    }

}