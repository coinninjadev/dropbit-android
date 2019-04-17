package com.coinninja.coinkeeper.util.uuid;

import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import javax.inject.Inject;

public class UuidFactory {
    final static String PREFERENCES_UUID = "PREFERENCES_UUID";

    private final PreferencesUtil preferencesUtil;
    private final UUIDGenerator uuidGenerator;

    @Inject
    UuidFactory(PreferencesUtil preferencesUtil, UUIDGenerator uuidGenerator) {
        this.preferencesUtil = preferencesUtil;
        this.uuidGenerator = uuidGenerator;
    }

    public String provideUuid() {
        String uuid = preferencesUtil.getString(PREFERENCES_UUID, "");

        if (uuid.isEmpty()) {
            uuid = uuidGenerator.generate();
            preferencesUtil.savePreference(PREFERENCES_UUID, uuid);
        }

        return uuid;
    }
}
