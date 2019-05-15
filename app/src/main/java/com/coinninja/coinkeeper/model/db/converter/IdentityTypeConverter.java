package com.coinninja.coinkeeper.model.db.converter;

import com.coinninja.coinkeeper.model.db.enums.IdentityType;

import org.greenrobot.greendao.converter.PropertyConverter;

public class IdentityTypeConverter implements PropertyConverter<IdentityType, Integer> {

    @Override
    public IdentityType convertToEntityProperty(Integer databaseValue) {
        if (databaseValue == null) {
            return IdentityType.UNKNOWN;
        }

        for (IdentityType type : IdentityType.values()) {
            if (type.getId() == databaseValue) {
                return type;
            }
        }

        return IdentityType.UNKNOWN;
    }

    @Override
    public Integer convertToDatabaseValue(IdentityType type) {
        return type == null ? null : type.getId();
    }
}
