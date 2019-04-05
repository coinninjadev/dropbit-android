package com.coinninja.coinkeeper.service.client.model;

public class InviteMetadata {
    MetadataAmount amount;
    MetadataContact sender;
    MetadataContact receiver;

    public MetadataAmount getAmount() {
        return amount;
    }

    public MetadataContact getSender() {
        return sender;
    }

    public MetadataContact getReceiver() {
        return receiver;
    }

    public void setAmount(MetadataAmount amount) {
        this.amount = amount;
    }

    public void setSender(MetadataContact sender) {
        this.sender = sender;
    }

    public void setReceiver(MetadataContact receiver) {
        this.receiver = receiver;
    }

    public static class MetadataContact {
        int country_code;
        String phone_number;

        public MetadataContact() {
        }

        public MetadataContact(int country_code, String phone_number) {
            this.country_code = country_code;
            this.phone_number = phone_number;
        }

        public int getCountry_code() {
            return country_code;
        }

        public String getPhone_number() {
            return phone_number;
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
