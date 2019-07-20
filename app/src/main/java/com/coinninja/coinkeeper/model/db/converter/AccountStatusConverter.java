package com.coinninja.coinkeeper.model.db.converter;

import com.coinninja.coinkeeper.model.db.enums.AccountStatus;

import org.greenrobot.greendao.converter.PropertyConverter;

public class AccountStatusConverter implements PropertyConverter<AccountStatus, Integer> {
    @Override
    public AccountStatus convertToEntityProperty(Integer databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        for (AccountStatus status : AccountStatus.values()) {
            if (status.getId() == databaseValue) {
                return status;
            }
        }

        return AccountStatus.UNVERIFIED;
    }

    @Override
    public Integer convertToDatabaseValue(AccountStatus status) {
        return status == null ? null : status.getId();
    }
}
