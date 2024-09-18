package com.aid.trader.presentation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aid.trader.data.payload.RequestPayload
import com.aid.trader.data.payload.ResponsePayload
import com.aid.trader.helper.NetworkCallResult
import com.aid.trader.interfaces.ForexRepositoryInterface
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel : ViewModel(), KoinComponent {
    private val forexRepository: ForexRepositoryInterface by inject()

    private val _response = MutableLiveData<NetworkCallResult<ResponsePayload>>()
    val response: LiveData<NetworkCallResult<ResponsePayload>> get() = _response

    fun updateForex(payload: RequestPayload) {
        viewModelScope.launch {
            forexRepository.updateForex(payload) { result ->
                _response.value = result
            }
        }
    }

}
