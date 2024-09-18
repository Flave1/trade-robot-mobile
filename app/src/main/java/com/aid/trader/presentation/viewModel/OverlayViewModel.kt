package com.aid.trader.presentation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aid.trader.data.payload.LLMForexChartResponse
import com.aid.trader.helper.NetworkCallResult
import com.aid.trader.interfaces.ForexRepositoryInterface
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class OverlayViewModel : ViewModel(), KoinComponent {
    private val forexRepository: ForexRepositoryInterface by inject()

    private val _response = MutableLiveData<NetworkCallResult<LLMForexChartResponse>>()
    val response: LiveData<NetworkCallResult<LLMForexChartResponse>> get() = _response

    fun submitChart(imageFile: File, currencyPairs: String, timeFrame: String, tradingStrategy: String) {
        viewModelScope.launch {
            forexRepository.submitChart(imageFile, currencyPairs, timeFrame, tradingStrategy) { result ->
                _response.value = result
            }
        }
    }
}
