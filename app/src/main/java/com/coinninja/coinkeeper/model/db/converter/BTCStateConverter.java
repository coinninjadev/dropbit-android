package com.coinninja.coinkeeper.model.db.converter;

import com.coinninja.coinkeeper.model.db.enums.BTCState;

import org.greenrobot.greendao.converter.PropertyConverter;

public class BTCStateConverter implements PropertyConverter<BTCState, Integer> {

    @Override
    public BTCState convertToEntityProperty(Integer databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        for (BTCState state : BTCState.values()) {
            if (state.getId() == databaseValue) {
                return state;
            }
        }

        return BTCState.UNFULFILLED;
    }

    @Override
    public Integer convertToDatabaseValue(BTCState state) {
        return state == null ? null : state.getId();
    }
}
