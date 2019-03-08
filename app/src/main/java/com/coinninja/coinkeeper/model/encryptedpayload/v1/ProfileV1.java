package com.coinninja.coinkeeper.model.encryptedpayload.v1;

import com.google.gson.annotations.SerializedName;

public class ProfileV1 {
    @SerializedName("display_name")
    private String displayName;

    @SerializedName("country_code")
    private int countryCode;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("dropbit_me")
    private String handle;

    private String avatar;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
