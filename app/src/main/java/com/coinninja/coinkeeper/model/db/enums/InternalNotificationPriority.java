package com.coinninja.coinkeeper.model.db.enums;

import org.greenrobot.greendao.converter.PropertyConverter;

public enum InternalNotificationPriority {

    _0_HIGHEST(0), _1(1), _2(2), _3(3), _4(4), _5(5), _6(6), _7(7), _8(8), _9(9), _10_LOWEST(10);

    private final int id;

    InternalNotificationPriority(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    public static class PriorityConverter implements PropertyConverter<InternalNotificationPriority, Integer> {
        @Override
        public InternalNotificationPriority convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }

            for (InternalNotificationPriority priority : InternalNotificationPriority.values()) {
                if (priority.id == databaseValue) {
                    return priority;
                }
            }

            return InternalNotificationPriority._10_LOWEST;
        }

        @Override
        public Integer convertToDatabaseValue(InternalNotificationPriority priority) {
            return priority == null ? null : priority.id;
        }
    }
}
