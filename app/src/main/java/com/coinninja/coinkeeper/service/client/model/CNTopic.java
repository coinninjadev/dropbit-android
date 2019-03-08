package com.coinninja.coinkeeper.service.client.model;

public class CNTopic {
    String id;

    public CNTopic() {
    }

    public CNTopic(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj == null || !(obj instanceof CNTopic)) return false;

        return id.equals(((CNTopic) obj).getId());
    }
}
