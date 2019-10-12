package app.coinninja.cn.persistance.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.persistance.DropbitDatabase
import app.coinninja.cn.persistance.model.LightningAccount
import app.dropbit.commons.currency.BTCCurrency
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LightningAccountDaoTest {
    lateinit var db: DropbitDatabase
    @get:Rule
    var instantTaskExecutor = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, DropbitDatabase::class.java).allowMainThreadQueries().build()
    }


    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun dao_can_insert_new_record() {
        assertThat(db.lightningAccountDao().getAccount()).isNull()
        val lightningAccount = LightningAccount(
                serverId = "--server-id",
                address = "--address--",
                createdAt = "--created-at--",
                updatedAt = "--updated-at--",
                balance = BTCCurrency(3000L),
                pendingIn = BTCCurrency(2000L),
                pendingOut = BTCCurrency(1000L)
        )

        db.lightningAccountDao().insert(lightningAccount)
        val account = db.lightningAccountDao().getAccount()!!
        assertThat(account.serverId).isEqualTo(lightningAccount.serverId)
        assertThat(account.address).isEqualTo(lightningAccount.address)
        assertThat(account.createdAt).isEqualTo(lightningAccount.createdAt)
        assertThat(account.updatedAt).isEqualTo(lightningAccount.updatedAt)
        assertThat(account.balance.toLong()).isEqualTo(lightningAccount.balance.toLong())
        assertThat(account.pendingIn.toLong()).isEqualTo(lightningAccount.pendingIn.toLong())
        assertThat(account.pendingOut.toLong()).isEqualTo(lightningAccount.pendingOut.toLong())
    }

    @Test
    fun dao_can_update_existing_account() {
        assertThat(db.lightningAccountDao().getAccount()).isNull()
        val lightningAccount = LightningAccount(
                serverId = "--server-id",
                address = "--address--",
                createdAt = "--created-at--",
                updatedAt = "--updated-at--",
                balance = BTCCurrency(3000L),
                pendingIn = BTCCurrency(2000L),
                pendingOut = BTCCurrency(1000L)
        )

        db.lightningAccountDao().insert(lightningAccount)
        val account = db.lightningAccountDao().getAccount()!!
        account.updatedAt = "updated"
        account.createdAt = "created"
        account.address = "address"
        account.serverId = "server"
        account.balance = BTCCurrency(30L)
        account.pendingIn = BTCCurrency(20L)
        account.pendingOut = BTCCurrency(10L)

        db.lightningAccountDao().update(account)
        val updated = db.lightningAccountDao().getAccount()!!
        assertThat(updated.serverId).isEqualTo(account.serverId)
        assertThat(updated.address).isEqualTo(account.address)
        assertThat(updated.createdAt).isEqualTo(account.createdAt)
        assertThat(updated.updatedAt).isEqualTo(account.updatedAt)
        assertThat(updated.balance.toLong()).isEqualTo(account.balance.toLong())
        assertThat(updated.pendingIn.toLong()).isEqualTo(account.pendingIn.toLong())
        assertThat(updated.pendingOut.toLong()).isEqualTo(account.pendingOut.toLong())
    }

    @Test
    fun dao_can_insert_when_account_does_not_exist__insert_or_update() {
        assertThat(db.lightningAccountDao().getAccount()).isNull()
        val lightningAccount = LightningAccount(
                serverId = "--server-id",
                address = "--address--",
                createdAt = "--created-at--",
                updatedAt = "--updated-at--",
                balance = BTCCurrency(3000L),
                pendingIn = BTCCurrency(2000L),
                pendingOut = BTCCurrency(1000L)
        )

        db.lightningAccountDao().insertOrUpdate(lightningAccount)

        val account = db.lightningAccountDao().getAccount()!!
        assertThat(account.serverId).isEqualTo(lightningAccount.serverId)
        assertThat(account.address).isEqualTo(lightningAccount.address)
        assertThat(account.createdAt).isEqualTo(lightningAccount.createdAt)
        assertThat(account.updatedAt).isEqualTo(lightningAccount.updatedAt)
        assertThat(account.balance.toLong()).isEqualTo(lightningAccount.balance.toLong())
        assertThat(account.pendingIn.toLong()).isEqualTo(lightningAccount.pendingIn.toLong())
        assertThat(account.pendingOut.toLong()).isEqualTo(lightningAccount.pendingOut.toLong())
    }

    @Test
    fun dao_can_updates_when_account_does_already_exist__insert_or_update() {
        assertThat(db.lightningAccountDao().getAccount()).isNull()
        val lightningAccount = LightningAccount(
                serverId = "--server-id",
                address = "--address--",
                createdAt = "--created-at--",
                updatedAt = "--updated-at--",
                balance = BTCCurrency(3000L),
                pendingIn = BTCCurrency(2000L),
                pendingOut = BTCCurrency(1000L)
        )
        db.lightningAccountDao().insert(lightningAccount)

        lightningAccount.updatedAt = "updated"
        lightningAccount.createdAt = "created"
        lightningAccount.address = "address"
        lightningAccount.serverId = "server"
        lightningAccount.balance = BTCCurrency(30L)
        lightningAccount.pendingIn = BTCCurrency(20L)
        lightningAccount.pendingOut = BTCCurrency(10L)

        db.lightningAccountDao().insertOrUpdate(lightningAccount)

        val account = db.lightningAccountDao().getAccount()!!
        assertThat(db.lightningAccountDao().all().size).isEqualTo(1)
        assertThat(account.serverId).isEqualTo(lightningAccount.serverId)
        assertThat(account.address).isEqualTo(lightningAccount.address)
        assertThat(account.createdAt).isEqualTo(lightningAccount.createdAt)
        assertThat(account.updatedAt).isEqualTo(lightningAccount.updatedAt)
        assertThat(account.balance.toLong()).isEqualTo(lightningAccount.balance.toLong())
        assertThat(account.pendingIn.toLong()).isEqualTo(lightningAccount.pendingIn.toLong())
        assertThat(account.pendingOut.toLong()).isEqualTo(lightningAccount.pendingOut.toLong())
    }
}