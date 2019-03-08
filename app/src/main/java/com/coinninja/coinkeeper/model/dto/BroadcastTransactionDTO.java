package com.coinninja.coinkeeper.model.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.coinninja.coinkeeper.model.UnspentTransactionHolder;
import com.coinninja.coinkeeper.service.client.model.Contact;

import java.util.Objects;

public class BroadcastTransactionDTO implements Parcelable {

    private Contact contact;
    private boolean isMemoShared;
    private String memo;
    private UnspentTransactionHolder holder;
    private String publicKey;

    public BroadcastTransactionDTO(UnspentTransactionHolder holder, Contact contact, boolean isMemoShared, String memo, String publicKey) {
        this.contact = contact;
        this.isMemoShared = isMemoShared;
        this.memo = memo;
        this.holder = holder;
        this.publicKey = publicKey;
    }

    public BroadcastTransactionDTO(UnspentTransactionHolder holder, Contact contact) {
        this(holder, contact, false, "", null);
    }

    protected BroadcastTransactionDTO(Parcel in) {
        contact = in.readParcelable(Contact.class.getClassLoader());
        isMemoShared = in.readByte() != 0;
        memo = in.readString();
        holder = in.readParcelable(UnspentTransactionHolder.class.getClassLoader());
        publicKey = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(contact, flags);
        dest.writeByte((byte) (isMemoShared ? 1 : 0));
        dest.writeString(memo);
        dest.writeParcelable(holder, flags);
        dest.writeString(publicKey);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BroadcastTransactionDTO> CREATOR = new Creator<BroadcastTransactionDTO>() {
        @Override
        public BroadcastTransactionDTO createFromParcel(Parcel in) {
            return new BroadcastTransactionDTO(in);
        }

        @Override
        public BroadcastTransactionDTO[] newArray(int size) {
            return new BroadcastTransactionDTO[size];
        }
    };

    public Contact getContact() {
        return contact;
    }

    public boolean isMemoShared() {
        return isMemoShared;
    }

    public String getMemo() {
        return memo;
    }

    public UnspentTransactionHolder getHolder() {
        return holder;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setMemoShared(boolean memoShared) {
        isMemoShared = memoShared;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setHolder(UnspentTransactionHolder holder) {
        this.holder = holder;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BroadcastTransactionDTO that = (BroadcastTransactionDTO) o;
        return isMemoShared == that.isMemoShared &&
                Objects.equals(contact, that.contact) &&
                Objects.equals(memo, that.memo) &&
                Objects.equals(holder, that.holder) &&
                Objects.equals(publicKey, that.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contact, isMemoShared, memo, holder, publicKey);
    }
}
