package com.coinninja.coinkeeper.model.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.coinninja.coinkeeper.service.client.model.InvitedContact;

import java.util.Objects;

public class CompletedInviteDTO extends PendingInviteDTO implements Parcelable {
    private InvitedContact invitedContact;

    public CompletedInviteDTO(PendingInviteDTO pendingInvite, InvitedContact invitedContact) {
        super(pendingInvite.getContact(), pendingInvite.getBitcoinPrice(),
                pendingInvite.getInviteAmount(), pendingInvite.getInviteFee(),
                pendingInvite.getMemo(), pendingInvite.isMemoIsShared());

        this.invitedContact = invitedContact;
    }

    protected CompletedInviteDTO(Parcel in) {
        super(in);
        invitedContact = in.readParcelable(InvitedContact.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(invitedContact, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CompletedInviteDTO> CREATOR = new Creator<CompletedInviteDTO>() {
        @Override
        public CompletedInviteDTO createFromParcel(Parcel in) {
            return new CompletedInviteDTO(in);
        }

        @Override
        public CompletedInviteDTO[] newArray(int size) {
            return new CompletedInviteDTO[size];
        }
    };

    public InvitedContact getInvitedContact() {
        return invitedContact;
    }

    public void setInvitedContact(InvitedContact invitedContact) {
        this.invitedContact = invitedContact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompletedInviteDTO that = (CompletedInviteDTO) o;
        return Objects.equals(invitedContact, that.invitedContact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), invitedContact);
    }

    public String getCnId() {
        return invitedContact.getId();
    }

    public boolean hasCnId() {
        return !"".equals(invitedContact.getId());
    }
}
