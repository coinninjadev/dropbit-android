package com.coinninja.coinkeeper.model.db.converter;

import android.net.Uri;

import org.greenrobot.greendao.converter.PropertyConverter;

public class UriConverter implements PropertyConverter<Uri, String> {

    @Override
    public Uri convertToEntityProperty(String databaseValue) {
        if (databaseValue == null || databaseValue.isEmpty()) {
            return null;
        }
        return Uri.parse(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(Uri entityProperty) {
        if (entityProperty == null || entityProperty.toString().isEmpty()) {
            return null;
        }
        return entityProperty.toString();
    }
}