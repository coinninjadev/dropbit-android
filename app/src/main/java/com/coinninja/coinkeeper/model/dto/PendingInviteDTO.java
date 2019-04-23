package com.coinninja.coinkeeper.model.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.uuid.UUIDGenerator;

import java.util.Objects;
import java.util.UUID;

public class PendingInviteDTO implements Parcelable {

    private Contact contact;
    private long bitcoinPrice;
    private long inviteAmount;
    private long inviteFee;
    private String memo = "";
    private boolean memoIsShared;
    private String requestId;

    public PendingInviteDTO(Contact contact, long bitcoinPrice, long inviteAmount, long inviteFee, String memo, boolean memoIsShared) {
        commonInit(contact, bitcoinPrice, inviteAmount, inviteFee, memo, memoIsShared);
        this.requestId = UUID.randomUUID().toString();
    }

    protected PendingInviteDTO(Contact contact, long bitcoinPrice, long inviteAmount, long inviteFee, String memo, boolean memoIsShared, String requestId) {
        commonInit(contact, bitcoinPrice, inviteAmount, inviteFee, memo, memoIsShared);
        this.requestId = requestId;
    }

    private void commonInit(Contact contact, long bitcoinPrice, long inviteAmount, long inviteFee, String memo, boolean memoIsShared) {
        this.contact = contact;
        this.bitcoinPrice = bitcoinPrice;
        this.inviteAmount = inviteAmount;
        this.inviteFee = inviteFee;
        this.memo = memo;
        this.memoIsShared = memoIsShared;
    }

    protected PendingInviteDTO(Parcel in) {
        contact = in.readParcelable(Contact.class.getClassLoader());
        bitcoinPrice = in.readLong();
        inviteAmount = in.readLong();
        inviteFee = in.readLong();
        memo = in.readString();
        memoIsShared = in.readByte() != 0;
        requestId = in.readString();
    }

    public static final Creator<PendingInviteDTO> CREATOR = new Creator<PendingInviteDTO>() {
        @Override
        public PendingInviteDTO createFromParcel(Parcel in) {
            return new PendingInviteDTO(in);
        }

        @Override
        public PendingInviteDTO[] newArray(int size) {
            return new PendingInviteDTO[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PendingInviteDTO that = (PendingInviteDTO) o;
        return bitcoinPrice == that.bitcoinPrice &&
                inviteAmount == that.inviteAmount &&
                inviteFee == that.inviteFee &&
                memoIsShared == that.memoIsShared &&
                Objects.equals(contact, that.contact) &&
                Objects.equals(memo, that.memo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contact, bitcoinPrice, inviteAmount, inviteFee, memo, memoIsShared, requestId);
    }

    public String getRequestId() {
        return requestId;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public long getBitcoinPrice() {
        return bitcoinPrice;
    }

    public long getInviteAmount() {
        return inviteAmount;
    }

    public boolean hasMemo() {
        return !"".equals(memo);
    }

    public long getInviteFee() {
        return inviteFee;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public boolean isMemoIsShared() {
        return memoIsShared;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(contact, flags);
        dest.writeLong(bitcoinPrice);
        dest.writeLong(inviteAmount);
        dest.writeLong(inviteFee);
        dest.writeString(memo);
        dest.writeByte((byte) (memoIsShared ? 1 : 0));
        dest.writeString(requestId);
    }
}
