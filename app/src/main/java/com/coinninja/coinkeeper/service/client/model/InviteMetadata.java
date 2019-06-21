package com.coinninja.coinkeeper.service.client.model;

public class InviteMetadata {
    MetadataAmount amount;
    MetadataContact sender;
    MetadataContact receiver;
    Boolean suppress;
    String request_id;

    public Boolean getSuppress() {
        return suppress;
    }

    public void setSuppress(Boolean suppress) {
        this.suppress = suppress;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
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
        String handle;

        public MetadataContact(String type, String identity) {
            this.type = type;
            this.identity = identity;
        }

        public MetadataContact(String type, String identity, String handle) {
            this.type = type;
            this.identity = identity;
            this.handle = handle;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
        }

        public String getHandle() {
            if (handle != null && handle.startsWith("@"))
                handle = handle.replaceFirst("@", "");
            return handle;
        }

        public void setHandle(String handle) {
            this.handle = handle;
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
