package app.coinninja.cn.thunderdome.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AccountResponseTest {

    @Test
    fun adapt_to_lightning_account_model() {
        val accountResponse = AccountResponse(
                "--id--",
                "--created-at--",
                "--updated-at--",
                "--address--",
                3000L,
                2000L,
                1000L,
                true
        )

        val lightningAccount = accountResponse.toLightningAccount()
        assertThat(lightningAccount.serverId).isEqualTo(accountResponse.id)
        assertThat(lightningAccount.createdAt).isEqualTo(accountResponse.createdAt)
        assertThat(lightningAccount.updatedAt).isEqualTo(accountResponse.updatedAt)
        assertThat(lightningAccount.address).isEqualTo(accountResponse.address)
        assertThat(lightningAccount.balance.toLong()).isEqualTo(accountResponse.balance)
        assertThat(lightningAccount.pendingIn.toLong()).isEqualTo(accountResponse.pendingIn)
        assertThat(lightningAccount.pendingOut.toLong()).isEqualTo(accountResponse.pendingOut)
        assertThat(lightningAccount.isLocked).isTrue()
    }


}