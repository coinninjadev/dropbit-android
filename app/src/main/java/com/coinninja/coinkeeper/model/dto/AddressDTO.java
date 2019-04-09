package com.coinninja.coinkeeper.model.dto;

import com.coinninja.coinkeeper.model.db.Address;

public class AddressDTO {

    private final Address address;
    private final String uncompressedPublicKey;

    public AddressDTO(Address address, String uncompressedPubKey) {
        this.address = address;
        this.uncompressedPublicKey = uncompressedPubKey;
    }

    public Address getWrappedAddress() {
        return address;
    }

    public String getUncompressedPublicKey() {
        return uncompressedPublicKey;
    }

    public String getDerivationPathString() {
        return String.format("M/49'/0'/0'/%s/%s", address.getChangeIndex(), address.getIndex());
    }
}
