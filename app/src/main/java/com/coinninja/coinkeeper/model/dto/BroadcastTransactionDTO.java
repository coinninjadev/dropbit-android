package com.coinninja.coinkeeper.model.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.service.client.model.Contact;

import java.util.Objects;

public class BroadcastTransactionDTO implements Parcelable {

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

    private TransactionData transactionData;
    private Contact contact;
    private boolean isMemoShared;
    private String memo;
    private String publicKey;

    public BroadcastTransactionDTO(TransactionData transactionData, Contact contact, boolean isMemoShared, String memo, String publicKey) {
        this.transactionData = transactionData;
        this.contact = contact;
        this.isMemoShared = isMemoShared;
        this.memo = memo;
        this.publicKey = publicKey;
    }

    public BroadcastTransactionDTO(TransactionData transactionData, Contact contact) {
        this(transactionData, contact, false, "", null);
    }

    protected BroadcastTransactionDTO(Parcel in) {
        contact = in.readParcelable(Contact.class.getClassLoader());
        isMemoShared = in.readByte() != 0;
        memo = in.readString();
        transactionData = in.readParcelable(TransactionData.class.getClassLoader());
        publicKey = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(contact, flags);
        dest.writeByte((byte) (isMemoShared ? 1 : 0));
        dest.writeString(memo);
        dest.writeParcelable(transactionData, flags);
        dest.writeString(publicKey);
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public boolean isMemoShared() {
        return isMemoShared;
    }

    public void setMemoShared(boolean memoShared) {
        isMemoShared = memoShared;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public TransactionData getTransactionData() {
        return transactionData;
    }

    public void setTransactionData(TransactionData transactionData) {
        this.transactionData = transactionData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contact, isMemoShared, memo, transactionData, publicKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BroadcastTransactionDTO that = (BroadcastTransactionDTO) o;
        return isMemoShared == that.isMemoShared &&
                Objects.equals(contact, that.contact) &&
                Objects.equals(memo, that.memo) &&
                Objects.equals(transactionData, that.transactionData) &&
                Objects.equals(publicKey, that.publicKey);
    }
}
