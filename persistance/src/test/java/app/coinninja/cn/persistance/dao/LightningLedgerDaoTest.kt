package app.coinninja.cn.persistance.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.persistance.DropbitDatabase
import app.coinninja.cn.persistance.model.*
import app.dropbit.commons.currency.BTCCurrency
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LightningLedgerDaoTest {
    lateinit var db: DropbitDatabase
    @get:Rule
    var instantTaskExecutor = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, DropbitDatabase::class.java).allowMainThreadQueries().build()
        givenAnAccount()
    }

    private fun givenAnAccount() {
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

    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun inserts() {
        val account = db.lightningAccountDao().getAccount()!!
        val invoice = LightningInvoice(
                accountId = account.id,
                serverId = "3228fe64710af215840ec2fa96a0cbde4ca9dd15e6931743fe68d0259cba432d:0",
                updatedAt = "2019-08-22T22:41:42.903254Z",
                createdAt = "2019-08-22T22:34:46.481530Z",
                expiresAt = "2019-08-23T22:34:46.481530Z",
                status = LedgerStatus.PENDING,
                direction = LedgerDirection.IN,
                type = LedgerType.LIGHTNING,
                value = BTCCurrency(476190),
                networkFee = BTCCurrency(200),
                processingFee = BTCCurrency(100),
                isHidden = false
        )

        assertThat(db.lightningInvoiceDao().all().size).isEqualTo(0)

        db.lightningInvoiceDao().insertOrUpdate(invoice)

        val invoices = db.lightningInvoiceDao().all()
        assertThat(invoices.size).isEqualTo(1)
        assertThat(invoices[0].accountId).isEqualTo(account.id)
        assertThat(invoices[0].serverId).isEqualTo(invoice.serverId)
        assertThat(invoices[0].updatedAt).isEqualTo(invoice.updatedAt)
        assertThat(invoices[0].createdAt).isEqualTo(invoice.createdAt)
        assertThat(invoices[0].expiresAt).isEqualTo(invoice.expiresAt)
        assertThat(invoices[0].status).isEqualTo(invoice.status)
        assertThat(invoices[0].type).isEqualTo(invoice.type)
        assertThat(invoices[0].direction).isEqualTo(invoice.direction)
        assertThat(invoices[0].value.toLong()).isEqualTo(invoice.value.toLong())
        assertThat(invoices[0].networkFee.toLong()).isEqualTo(invoice.networkFee.toLong())
        assertThat(invoices[0].processingFee.toLong()).isEqualTo(invoice.processingFee.toLong())
        assertThat(invoices[0].memo).isEqualTo(invoice.memo)
        assertThat(invoices[0].error).isEqualTo(invoice.error)
    }


    @Test
    fun updates() {
        val account = db.lightningAccountDao().getAccount()!!
        val invoice = LightningInvoice(
                accountId = account.id,
                serverId = "3228fe64710af215840ec2fa96a0cbde4ca9dd15e6931743fe68d0259cba432d:0",
                updatedAt = "2019-08-22T22:41:42.903254Z",
                createdAt = "2019-08-22T22:34:46.481530Z",
                expiresAt = "2019-08-23T22:34:46.481530Z",
                status = LedgerStatus.PENDING,
                direction = LedgerDirection.IN,
                type = LedgerType.LIGHTNING,
                value = BTCCurrency(476190),
                networkFee = BTCCurrency(200),
                processingFee = BTCCurrency(100),
                isHidden = false
        )
        assertThat(db.lightningInvoiceDao().all().size).isEqualTo(0)
        db.lightningInvoiceDao().insert(invoice)
        assertThat(db.lightningInvoiceDao().all().size).isEqualTo(1)

        invoice.apply {
            memo = "foo"
            status = LedgerStatus.COMPLETED
        }

        db.lightningInvoiceDao().insertOrUpdate(invoice)

        val invoices = db.lightningInvoiceDao().all()
        assertThat(invoices.size).isEqualTo(1)
        assertThat(invoices[0].accountId).isEqualTo(account.id)
        assertThat(invoices[0].serverId).isEqualTo(invoice.serverId)
        assertThat(invoices[0].updatedAt).isEqualTo(invoice.updatedAt)
        assertThat(invoices[0].createdAt).isEqualTo(invoice.createdAt)
        assertThat(invoices[0].expiresAt).isEqualTo(invoice.expiresAt)
        assertThat(invoices[0].status).isEqualTo(LedgerStatus.COMPLETED)
        assertThat(invoices[0].type).isEqualTo(invoice.type)
        assertThat(invoices[0].direction).isEqualTo(invoice.direction)
        assertThat(invoices[0].value.toLong()).isEqualTo(invoice.value.toLong())
        assertThat(invoices[0].networkFee.toLong()).isEqualTo(invoice.networkFee.toLong())
        assertThat(invoices[0].processingFee.toLong()).isEqualTo(invoice.processingFee.toLong())
        assertThat(invoices[0].memo).isEqualTo("foo")
        assertThat(invoices[0].error).isEqualTo(invoice.error)
    }
}