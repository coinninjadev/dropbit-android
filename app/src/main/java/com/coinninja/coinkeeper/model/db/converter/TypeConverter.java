package com.coinninja.coinkeeper.model.db.converter;

import com.coinninja.coinkeeper.model.db.enums.Type;

import org.greenrobot.greendao.converter.PropertyConverter;

public class TypeConverter implements PropertyConverter<Type, Integer> {
    @Override
    public Type convertToEntityProperty(Integer databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        for (Type type : Type.values()) {
            if (type.getId() == databaseValue) {
                return type;
            }
        }

        return Type.BLOCKCHAIN_SENT;
    }

    @Override
    public Integer convertToDatabaseValue(Type type) {
        return type == null ? null : type.getId();
    }
}
