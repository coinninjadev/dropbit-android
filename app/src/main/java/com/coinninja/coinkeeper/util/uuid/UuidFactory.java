package com.coinninja.coinkeeper.util.uuid;

import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import javax.inject.Inject;

public class UuidFactory {
    final static String PREFERENCES_UUID = "PREFERENCES_UUID";

    private final UserHelper userHelper;
    private final PreferencesUtil preferencesUtil;
    private final UUIDGenerator uuidGenerator;

    @Inject
    UuidFactory(UserHelper userHelper, PreferencesUtil preferencesUtil, UUIDGenerator uuidGenerator) {
        this.userHelper = userHelper;
        this.preferencesUtil = preferencesUtil;
        this.uuidGenerator = uuidGenerator;
    }

    //TODO delete userhelper.getUniqueID after users on 1.0.5
    public String provideUuid() {
        String uuid = preferencesUtil.getString(PREFERENCES_UUID, "");

        if (uuid.isEmpty()) {
            uuid = userHelper.getUniqueID();

            if (uuid == null || uuid.isEmpty()) {
                uuid = uuidGenerator.generate();
            }

            preferencesUtil.savePreference(PREFERENCES_UUID, uuid);
        }

        return uuid;
    }
}
