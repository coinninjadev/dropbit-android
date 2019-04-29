package com.coinninja.coinkeeper.service.client.model;

import com.coinninja.coinkeeper.model.PhoneNumber;

public class InviteMetadata {
    MetadataAmount amount;
    MetadataContact sender;
    MetadataContact receiver;
    String request_id;

    public String getRequest_id() {
        return request_id;
    }

    public MetadataAmount getAmount() {
        return amount;
    }

    public void setAmount(MetadataAmount amount) {
        this.amount = amount;
    }

    public MetadataContact getSender() {
        return sender;
    }

    public void setSender(MetadataContact sender) {
        this.sender = sender;
    }

    public MetadataContact getReceiver() {
        return receiver;
    }

    public void setReceiver(MetadataContact receiver) {
        this.receiver = receiver;
    }

    public static class MetadataContact {
        String type;
        String identity;
        int country_code;
        String phone_number;

        public MetadataContact() {
        }

        public MetadataContact(int country_code, String phone_number) {
            this.country_code = country_code;
            this.phone_number = phone_number;
        }

        @Deprecated
        public int getCountry_code() {
            return country_code;
        }

        @Deprecated()
        public String getPhone_number() {
            return phone_number;
        }

        public String getType() {
            return type;
        }

        public String getIdentity() {
            return identity;
        }

        public PhoneNumber identityAsPhoneNumber() {
            if (null == phone_number) {
                return new PhoneNumber(String.format("+%s", identity));
            }
            return new PhoneNumber(country_code, phone_number);
        }
    }

    public static class MetadataAmount {
        long btc;
        long usd;

        public MetadataAmount() {
        }

        public MetadataAmount(long btc, long usd) {
            this.btc = btc;
            this.usd = usd;
        }

        public long getBtc() {
            return btc;
        }

        public long getUsd() {
            return usd;
        }
    }

}
