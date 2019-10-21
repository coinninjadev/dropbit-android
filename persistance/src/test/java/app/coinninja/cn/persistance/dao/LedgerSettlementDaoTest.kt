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
import org.junit.*
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class LedgerSettlementDaoTest {
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

    private fun given_a_settlement(invoice: LightningInvoice? = null) {
        val ledger = invoice?.let {
            db.lightningInvoiceDao().insertOrUpdate(it)
            db.lightningInvoiceDao().ledgerByServerId(invoice.serverId)
        }

        val settlement = LedgerSettlement(createdAt = Date(System.currentTimeMillis()))
        settlement.invoiceId = ledger?.id
        settlement.createdAt = invoice?.createdAt
        db.ledgerSettlementDao.insert(settlement)
    }


    @Test
    fun returns_settlement_containing_invoice_data() {
        val invoice1 = LightningInvoice(
                value = BTCCurrency(10_000),
                networkFee = BTCCurrency(1_000),
                processingFee = BTCCurrency(100),
                direction = LedgerDirection.OUT,
                type = LedgerType.LIGHTNING,
                status = LedgerStatus.COMPLETED,
                createdAt = Date(System.currentTimeMillis()),
                serverId = "--server-id-1--",
                memo = "--memo-1--"
        )
        given_a_settlement(invoice1)

        val settlements = db.ledgerSettlementDao.allDetails()
        assertThat(settlements.isEmpty()).isFalse()
        assertThat(settlements.size).isEqualTo(1)
        //assertThat(settlements.get(0).invoiceServerId).isEqualTo(invoice1.serverId)
        assertThat(settlements.get(0).invoiceValue).isEqualTo(invoice1.value.toLong())
        assertThat(settlements.get(0).invoiceNetworkFee).isEqualTo(invoice1.networkFee.toLong())
        assertThat(settlements.get(0).invoiceProcessingFee).isEqualTo(invoice1.processingFee.toLong())
        assertThat(settlements.get(0).invoiceDirection).isEqualTo(invoice1.direction)
        assertThat(settlements.get(0).invoiceType).isEqualTo(invoice1.type)
        assertThat(settlements.get(0).invoiceStatus).isEqualTo(invoice1.status)
        assertThat(settlements.get(0).invoiceCreatedAt).isEqualTo(invoice1.createdAt)
        assertThat(settlements.get(0).invoiceMemo).isEqualTo(invoice1.memo)
    }


    @Test
    fun returns_settlement_visible_invoices() {
        val invoice1 = LightningInvoice(
                value = BTCCurrency(10_000),
                networkFee = BTCCurrency(1_000),
                processingFee = BTCCurrency(100),
                direction = LedgerDirection.OUT,
                type = LedgerType.LIGHTNING,
                status = LedgerStatus.COMPLETED,
                createdAt = Date(System.currentTimeMillis()),
                serverId = "--server-id-1--",
                memo = "--memo-1--",
                isHidden = false

        )
        given_a_settlement(invoice1)

        given_a_settlement(LightningInvoice(
                value = BTCCurrency(15_000),
                networkFee = BTCCurrency(1_500),
                processingFee = BTCCurrency(500),
                direction = LedgerDirection.IN,
                type = LedgerType.LIGHTNING,
                status = LedgerStatus.EXPIRED,
                createdAt = Date(System.currentTimeMillis()),
                serverId = "--server-id-2--",
                memo = "--memo-2--",
                isHidden = true)
        )

        val settlements = db.ledgerSettlementDao.allVisible()
        assertThat(settlements.isEmpty()).isFalse()
        assertThat(settlements.size).isEqualTo(1)
        assertThat(db.ledgerSettlementDao.all().size).isEqualTo(2)
        //assertThat(settlements.get(0).invoiceServerId).isEqualTo(invoice1.serverId)
    }

    @Test
    fun returns_settlement_by_invoice_id() {
        var invoice = LightningInvoice(
                value = BTCCurrency(10_000),
                networkFee = BTCCurrency(1_000),
                processingFee = BTCCurrency(100),
                direction = LedgerDirection.OUT,
                type = LedgerType.LIGHTNING,
                status = LedgerStatus.COMPLETED,
                createdAt = Date(System.currentTimeMillis()),
                serverId = "--server-id-1--",
                memo = "--memo-1--",
                isHidden = false

        )
        given_a_settlement(invoice)
        invoice = db.lightningInvoiceDao().ledgerByServerId(invoice.serverId)!!

        assertThat(db.ledgerSettlementDao.settlementByInvoiceId(invoice.id)).isNotNull()
    }

    @Ignore
    @Test
    fun creates_settlement_for_pending_invite() {
        val invite = InviteTransactionSummary(

        )
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun can_insert_or_update() {
    }
}