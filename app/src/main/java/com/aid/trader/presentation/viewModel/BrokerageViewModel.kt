package com.aid.trader.presentation.viewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aid.trader.data.firebase.BrokerageInformation
import com.aid.trader.data.payload.ResponsePayload
import com.aid.trader.helper.NetworkCallResult
import com.aid.trader.interfaces.BrokerRepositoryInterface
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BrokerageViewModel : ViewModel(), KoinComponent {

    private val repository : BrokerRepositoryInterface by inject()
    private val _responseObject = MutableLiveData<NetworkCallResult<ResponsePayload>>()
    private val _responseList = MutableLiveData<List<BrokerageInformation>>()
    val responseObject: LiveData<NetworkCallResult<ResponsePayload>> get() = _responseObject
    val responseList: LiveData<List<BrokerageInformation>> get() = _responseList

    fun addBrokerage(brokerage: BrokerageInformation) {
        repository.addBrokerageInformation(brokerage)
    }

    fun verifyBrokerage(brokerage: BrokerageInformation) {
        repository.verifyBroker(brokerage) { result ->
            _responseObject.value = result
        }
    }

    fun getBrokerage(documentId: String) {
        repository.getBrokerageInformation(documentId)
    }

    fun updateBrokerage(documentId: String, brokerage: BrokerageInformation) {
        repository.updateBrokerageInformation(documentId, brokerage)
    }

    fun deleteBrokerage(documentId: String) {
        repository.deleteBrokerageInformation(documentId)
    }

    fun getListOfBrokerages() {
        try {
            repository.getBrokerageList { result ->
                _responseList.value = result
            }
        } catch (e: Exception) {
            _responseObject.value = NetworkCallResult.Error(e)
        }
    }
}
