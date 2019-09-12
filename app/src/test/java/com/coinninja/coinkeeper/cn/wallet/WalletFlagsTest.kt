package com.coinninja.coinkeeper.cn.wallet

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WalletFlagsTest {

    @Test
    fun purpose49_version0_active() {

        val flags = WalletFlags.compose(WalletFlags.purpose49, WalletFlags.v0)

        assertThat(flags.flag).isEqualTo(0)
        assertThat(flags.versionBit).isEqualTo(0)
        assertThat(flags.purposeBit).isEqualTo(0)
        assertThat(flags.hasPurpose(WalletFlags.purpose49)).isTrue()
        assertThat(flags.hasVersion(WalletFlags.v0)).isTrue()
        assertThat(flags.hasVersion(WalletFlags.v1)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v2)).isFalse()
        assertThat(flags.isActive()).isTrue()
    }

    @Test
    fun purpose49_version1_active() {
        val flags = WalletFlags.compose(WalletFlags.purpose49, WalletFlags.v1)

        assertThat(flags.flag).isEqualTo(1)
        assertThat(flags.versionBit).isEqualTo(1)
        assertThat(flags.purposeBit).isEqualTo(0)
        assertThat(flags.hasPurpose(WalletFlags.purpose49)).isTrue()
        assertThat(flags.hasVersion(WalletFlags.v0)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v1)).isTrue()
        assertThat(flags.hasVersion(WalletFlags.v2)).isFalse()
        assertThat(flags.isActive()).isTrue()
    }

    @Test
    fun purpose49_version2_active() {
        val flags = WalletFlags.compose(WalletFlags.purpose49, WalletFlags.v2)

        assertThat(flags.flag).isEqualTo(2)
        assertThat(flags.versionBit).isEqualTo(2)
        assertThat(flags.purposeBit).isEqualTo(0)
        assertThat(flags.hasPurpose(WalletFlags.purpose49)).isTrue()
        assertThat(flags.hasVersion(WalletFlags.v0)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v1)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v2)).isTrue()
        assertThat(flags.isActive()).isTrue()
    }

    @Test
    fun purpose84_version0_active() {
        val flags = WalletFlags.compose(WalletFlags.purpose84, WalletFlags.v0)

        assertThat(flags.flag).isEqualTo(16)
        assertThat(flags.versionBit).isEqualTo(0)
        assertThat(flags.purposeBit).isEqualTo(1)
        assertThat(flags.hasVersion(WalletFlags.v0)).isTrue()
        assertThat(flags.hasVersion(WalletFlags.v1)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v2)).isFalse()
        assertThat(flags.hasPurpose(WalletFlags.purpose84)).isTrue()
        assertThat(flags.isActive()).isTrue()
    }

    @Test
    fun purpose84_version1_active() {
        val flags = WalletFlags.compose(WalletFlags.purpose84, WalletFlags.v1)

        assertThat(flags.flag).isEqualTo(17)
        assertThat(flags.versionBit).isEqualTo(1)
        assertThat(flags.purposeBit).isEqualTo(1)
        assertThat(flags.hasVersion(WalletFlags.v0)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v1)).isTrue()
        assertThat(flags.hasVersion(WalletFlags.v2)).isFalse()
        assertThat(flags.hasPurpose(WalletFlags.purpose84)).isTrue()
        assertThat(flags.isActive()).isTrue()
    }

    @Test
    fun purpose84_version2_active() {
        val flags = WalletFlags.compose(WalletFlags.purpose84, WalletFlags.v2)
        assertThat(flags.flag).isEqualTo(18)
        assertThat(flags.versionBit).isEqualTo(2)
        assertThat(flags.purposeBit).isEqualTo(1)
        assertThat(flags.hasVersion(WalletFlags.v0)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v1)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v2)).isTrue()
        assertThat(flags.hasPurpose(WalletFlags.purpose84)).isTrue()
        assertThat(flags.isActive()).isTrue()
    }

    @Test
    fun not_active_version_0_purpose_49() {
        val flags = WalletFlags.compose(WalletFlags.purpose84, WalletFlags.v2, false)
        assertThat(flags.flag).isEqualTo(274)
        assertThat(flags.versionBit).isEqualTo(2)
        assertThat(flags.purposeBit).isEqualTo(1)
        assertThat(flags.isActive()).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v0)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v1)).isFalse()
        assertThat(flags.hasVersion(WalletFlags.v2)).isTrue()
        assertThat(flags.hasPurpose(WalletFlags.purpose84)).isTrue()
    }

    @Test
    fun getter_for_purpose49_version1() {
        assertThat(WalletFlags.purpose49v1).isEqualTo(1)
    }

    @Test
    fun getter_for_purpose84_version2() {
        assertThat(WalletFlags.purpose84v2).isEqualTo(18)
    }
}
