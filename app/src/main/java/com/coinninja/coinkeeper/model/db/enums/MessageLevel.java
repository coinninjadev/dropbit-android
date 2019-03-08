package com.coinninja.coinkeeper.model.db.enums;

import org.greenrobot.greendao.converter.PropertyConverter;

public enum MessageLevel {
    INFO(0, "info"),
    SUCCESS(1, "success"),
    WARN(2, "warn"),
    ERROR(3, "error");

    private final static MessageLevel mDefault = MessageLevel.INFO;

    private final int id;
    private final String stringValue;

    MessageLevel(int id, String stringValue) {
        this.id = id;
        this.stringValue = stringValue;
    }

    public static MessageLevel getDefault() {
        return mDefault;
    }

    public int getId() {
        return id;
    }

    public String getStringValue() {
        return stringValue;
    }


    public static class Converter implements PropertyConverter<MessageLevel, Integer> {
        @Override
        public MessageLevel convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }

            for (MessageLevel messageLevel : MessageLevel.values()) {
                if (messageLevel.getId() == databaseValue) {
                    return messageLevel;
                }
            }

            return MessageLevel.getDefault();
        }

        @Override
        public Integer convertToDatabaseValue(MessageLevel messageLevel) {
            return messageLevel == null ? null : messageLevel.getId();
        }

        public MessageLevel fromString(String level) {
            for (MessageLevel messageLevel : MessageLevel.values()) {
                if (messageLevel.stringValue.toLowerCase().contentEquals(level.toLowerCase())) {
                    return messageLevel;
                }
            }

            return MessageLevel.getDefault();
        }
    }
}
