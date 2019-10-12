package com.coinninja.coinkeeper.ui.lightning.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import app.coinninja.cn.persistance.model.LedgerSettlement
import app.coinninja.cn.persistance.model.LedgerSettlementDetail
import app.coinninja.cn.persistance.model.LightningInvoice
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable

@Mockable
class LightningHistoryViewModel(val thunderDomeRepository: ThunderDomeRepository) : ViewModel() {

    fun loadInvoices(): LiveData<List<LedgerSettlementDetail>> {
        return thunderDomeRepository.visibleSettlements
    }

}
