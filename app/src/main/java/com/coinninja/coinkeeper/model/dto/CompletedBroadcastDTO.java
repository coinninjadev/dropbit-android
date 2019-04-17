package com.coinninja.coinkeeper.model.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.service.client.model.Contact;

import java.util.Objects;

public class CompletedBroadcastDTO extends BroadcastTransactionDTO implements Parcelable {

    public static final Creator<CompletedBroadcastDTO> CREATOR = new Creator<CompletedBroadcastDTO>() {
        @Override
        public CompletedBroadcastDTO createFromParcel(Parcel in) {
            return new CompletedBroadcastDTO(in);
        }

        @Override
        public CompletedBroadcastDTO[] newArray(int size) {
            return new CompletedBroadcastDTO[size];
        }
    };
    public String transactionId;

    public CompletedBroadcastDTO(BroadcastTransactionDTO broadcastActivityDTO, String transactionId) {
        super(broadcastActivityDTO.getTransactionData(), broadcastActivityDTO.getContact(),
                broadcastActivityDTO.isMemoShared(), broadcastActivityDTO.getMemo(),
                broadcastActivityDTO.getPublicKey());
        this.transactionId = transactionId;
    }

    public CompletedBroadcastDTO(TransactionData transactionData, String transactionId, Contact contact) {
        super(transactionData, contact);
        this.transactionId = transactionId;
    }

    protected CompletedBroadcastDTO(Parcel in) {
        super(in);
        transactionId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transactionId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompletedBroadcastDTO that = (CompletedBroadcastDTO) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    public boolean shouldShareMemo() {
        return getContact() != null && getContact() != null && !"".equals(getMemo()) && isMemoShared()
                && !"".equals(getPublicKey());
    }

    public boolean hasMemo() {
        return getMemo() != null && !"".equals(getMemo());
    }

    public String getPhoneNumberHash() {
        return getContact().getHash();
    }

    public boolean hasPublicKey() {
        return !"".equals(getPublicKey());
    }
}
