package com.aid.trader.interfaces

import com.aid.trader.data.payload.LLMForexChartResponse
import com.aid.trader.data.payload.RequestPayload
import com.aid.trader.data.payload.ResponsePayload
import com.aid.trader.helper.NetworkCallResult
import java.io.File

interface ForexRepositoryInterface {
    fun updateForex(payload: RequestPayload, callback: (NetworkCallResult<ResponsePayload>) -> Unit)
    fun submitChart(imageFile: File, currencyPairs: String, timeFrame: String, tradingStrategy: String, callback: (NetworkCallResult<LLMForexChartResponse>) -> Unit)
}
