package com.coinninja.coinkeeper.model.dto;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.coinkeeper.model.db.Address;

import java.util.Objects;

public class AddressDTO {

    private final String address;
    private final String derivationPath;
    private final String uncompressedPublicKey;

    public static String toDerivationPathString(DerivationPath path) {
        return String.format("M/49/0/0/%s/%s", path.getChange(), path.getIndex());
    }

    @Deprecated
    public AddressDTO(Address address, String uncompressedPubKey) {
        this(address.getAddress(), toDerivationPathString(address.getDerivationPath()), uncompressedPubKey);
    }

    public AddressDTO(String address, String derivationPath, String uncompressedPublicKey) {
        this.address = address;
        this.derivationPath = derivationPath;
        this.uncompressedPublicKey = uncompressedPublicKey;
    }

    public String getDerivationPath() {
        return derivationPath;
    }

    public String getAddress() {
        return address;
    }

    public int getIndex() {
        return new DerivationPath(derivationPath).getIndex();
    }

    public String getUncompressedPublicKey() {
        return uncompressedPublicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressDTO that = (AddressDTO) o;
        return Objects.equals(address, that.address) &&
                Objects.equals(derivationPath, that.derivationPath) &&
                Objects.equals(uncompressedPublicKey, that.uncompressedPublicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, derivationPath, uncompressedPublicKey);
    }
}
