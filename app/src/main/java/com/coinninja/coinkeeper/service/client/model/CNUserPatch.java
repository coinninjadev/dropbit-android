package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

public class CNUserPatch {

    @SerializedName("private")
    private boolean isPrivate;

    public CNUserPatch() {

    }

    public CNUserPatch(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}
