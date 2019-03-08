package com.coinninja.coinkeeper.model.db.converter;

import com.coinninja.coinkeeper.model.db.enums.MemPoolState;

import org.greenrobot.greendao.converter.PropertyConverter;

public class MemPoolStateConverter implements PropertyConverter<MemPoolState, Integer> {
    @Override
    public MemPoolState convertToEntityProperty(Integer databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        for (MemPoolState state : MemPoolState.values()) {
            if (state.getId() == databaseValue) {
                return state;
            }
        }

        return MemPoolState.PENDING;
    }

    @Override
    public Integer convertToDatabaseValue(MemPoolState state) {
        return state == null ? null : state.getId();
    }
}
