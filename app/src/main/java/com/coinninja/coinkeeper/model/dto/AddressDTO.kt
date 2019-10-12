package com.coinninja.coinkeeper.model.dto

import app.coinninja.cn.libbitcoin.model.DerivationPath
import com.coinninja.coinkeeper.model.db.Address
import java.util.*

class AddressDTO(val address: String, val derivationPath: String, val uncompressedPublicKey: String) {

    val index: Int get() = DerivationPath.from(derivationPath).index

    constructor(address: Address, uncompressedPubKey: String) : this(address.address, address.derivationPath.toString(), uncompressedPubKey)

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

}
