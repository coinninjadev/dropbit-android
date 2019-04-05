package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;

class InvitationResult {
    String errorMessage;
    Status status;
    DropBitInvitation invitedContact;
    private int statusCode;

    public InvitationResult(DropBitInvitation invitedContact, int statusCode, Status status, String errorMessage) {
        this.invitedContact = invitedContact;
        this.statusCode = statusCode;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public InvitationResult(DropBitInvitation inviteContact, int code, Status status) {
        this(inviteContact, code, status, "");
    }

    public InvitationResult(int code, Status status, String errorMessage) {
        this(null, code, status, errorMessage);
    }

    public InvitationResult(Status status, String errorMessage) {
        this(null, 0, status, errorMessage);
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public DropBitInvitation getInvitedResult() {
        return invitedContact;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public enum Status {
        UNKNOWN_ERROR, SUCCESS, ERROR, DEGRADED_SMS
    }
}
