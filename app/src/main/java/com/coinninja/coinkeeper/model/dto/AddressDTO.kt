package com.coinninja.coinkeeper.model.dto

import com.coinninja.bindings.DerivationPath
import com.coinninja.coinkeeper.model.db.Address

import java.util.Objects

class AddressDTO(val address: String, val derivationPath: String, val uncompressedPublicKey: String) {

    val index: Int
        get() = DerivationPath(derivationPath).index!!

    constructor(address: Address, uncompressedPubKey: String) : this(address.address, toDerivationPathString(address.derivationPath), uncompressedPubKey) {}

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as AddressDTO?
        return address == that!!.address &&
                derivationPath == that.derivationPath &&
                uncompressedPublicKey == that.uncompressedPublicKey
    }

    override fun hashCode(): Int {
        return Objects.hash(address, derivationPath, uncompressedPublicKey)
    }

    companion object {

        fun toDerivationPathString(path: DerivationPath): String {
            return String.format("M/%s/%s/%s/%s/%s", path.purpose, path.coinType, path.account, path.change, path.index)
        }
    }
}
