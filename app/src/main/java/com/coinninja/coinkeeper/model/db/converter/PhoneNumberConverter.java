package com.coinninja.coinkeeper.model.db.converter;

import com.coinninja.coinkeeper.model.db.PhoneNumber;

import org.greenrobot.greendao.converter.PropertyConverter;

public class PhoneNumberConverter implements PropertyConverter<PhoneNumber, String> {

    @Override
    public PhoneNumber convertToEntityProperty(String databaseValue) {
        if (null == databaseValue || databaseValue.equals("")) {
            return null;
        }
        return new PhoneNumber(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(PhoneNumber entityProperty) {
        return entityProperty == null ? null : entityProperty.toString();
    }
}
