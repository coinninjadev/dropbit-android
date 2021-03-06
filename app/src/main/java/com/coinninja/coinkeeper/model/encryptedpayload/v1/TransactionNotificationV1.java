package com.coinninja.coinkeeper.model.encryptedpayload.v1;

import com.google.gson.Gson;

import androidx.annotation.NonNull;

import java.util.Objects;

public class TransactionNotificationV1 {

    private MetaV1 meta;
    private String txid;
    private InfoV1 info;
    private ProfileV1 profile;

    /* Sample V1 Payload
   {
     "meta": {
       "version": 1
     },
     "txid": "....",
     "info": {
       "memo": "Here's your 5 dollars 💸",
       "amount": 500,
       "currency": "USD"
     },
     "profile": {
       "display_name": "",
       "country_code": 1,
       "phone_number": "3305551122",
       "dropbit_me": "",
       "avatar": "aW5zZXJ0IGF2YXRhciBoZXJlCg=="
     }
   }
 */

    public MetaV1 getMeta() {
        return meta;
    }

    public void setMeta(MetaV1 meta) {
        this.meta = meta;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public InfoV1 getInfo() {
        return info;
    }

    public void setInfo(InfoV1 info) {
        this.info = info;
    }

    public ProfileV1 getProfile() {
        return profile;
    }

    public void setProfile(ProfileV1 profile) {
        this.profile = profile;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionNotificationV1 that = (TransactionNotificationV1) o;
        return Objects.equals(meta, that.meta) &&
                Objects.equals(txid, that.txid) &&
                Objects.equals(info, that.info) &&
                Objects.equals(profile, that.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meta, txid, info, profile);
    }
}
