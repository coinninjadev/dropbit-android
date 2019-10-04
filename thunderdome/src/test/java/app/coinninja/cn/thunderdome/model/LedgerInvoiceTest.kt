package app.coinninja.cn.thunderdome.model

import app.coinninja.cn.persistance.model.LedgerDirection
import app.coinninja.cn.persistance.model.LedgerStatus
import app.coinninja.cn.persistance.model.LedgerType
import app.coinninja.cn.thunderdome.client.Testdata
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Test


class LedgerInvoiceTest {

    @Test
    fun converts_to_db_model() {
        val ledgerResponse = Gson().fromJson(Testdata.invoices, LedgerResponse::class.java)

        val invoices = ledgerResponse.invoices
        val invoice = invoices[0].toLightningLedger()
        val invoice2 = invoices[1].toLightningLedger()

        assertThat(invoices[0].id).isEqualTo("3228fe64710af215840ec2fa96a0cbde4ca9dd15e6931743fe68d0259cba432d:0")
        assertThat(invoice.serverId).isEqualTo("3228fe64710af215840ec2fa96a0cbde4ca9dd15e6931743fe68d0259cba432d")
        assertThat(invoice.createdAt!!.time).isEqualTo(1566513767530)
        assertThat(invoice.updatedAt!!.time).isEqualTo(1566514605254)
        assertThat(invoice.expiresAt).isNull()
        assertThat(invoice.status).isEqualTo(LedgerStatus.COMPLETED)
        assertThat(invoice.type).isEqualTo(LedgerType.BTC)
        assertThat(invoice.direction).isEqualTo(LedgerDirection.IN)
        assertThat(invoice.value.toLong()).isEqualTo(476190)
        assertThat(invoice.networkFee.toLong()).isEqualTo(10)
        assertThat(invoice.processingFee.toLong()).isEqualTo(200)
        assertThat(invoice.memo).isEqualTo("")
        assertThat(invoice.addIndex).isNull()
        assertThat(invoice.request).isEqualTo("")
        assertThat(invoice.error).isEqualTo("")
        assertThat(invoice.isHidden).isFalse()
        assertThat(invoice.isGenerated).isFalse()

        assertThat(invoices[1].id).isEqualTo("3228fe64710af215840ec2fa96a0cbde4ca9dd15e6931743fe68d0259cba432d")
        assertThat(invoice2.serverId).isEqualTo("3228fe64710af215840ec2fa96a0cbde4ca9dd15e6931743fe68d0259cba432d")
    }


}