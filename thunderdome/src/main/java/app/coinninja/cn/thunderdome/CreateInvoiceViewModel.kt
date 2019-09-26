package app.coinninja.cn.thunderdome

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Mockable
class CreateInvoiceViewModel(
        val thunderDomeRepository: ThunderDomeRepository
) : ViewModel() {

    val request: MutableLiveData<String?> = MutableLiveData()

    fun createInvoiceFor(amount: Long, memo: String? = null) {
        viewModelScope.launch {
            updateRequest(createInvoice(amount, memo))
        }
    }

    private suspend fun updateRequest(requestValue: String?) = withContext(Dispatchers.Main) {
        request.value = requestValue

    }

    private suspend fun createInvoice(amount: Long, memo: String?): String? = withContext(Dispatchers.IO) {
        thunderDomeRepository.createInvoiceFor(amount, memo)
    }

}