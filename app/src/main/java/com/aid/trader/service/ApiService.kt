package com.aid.trader.service


import com.aid.trader.data.firebase.BrokerageInformation
import com.aid.trader.data.payload.LLMForexChartResponse
import com.aid.trader.data.payload.RequestPayload
import com.aid.trader.data.payload.ResponsePayload
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("api/v1/forex/update")
    fun updateForex(@Body payload: RequestPayload): Call<ResponsePayload>

    @Multipart
    @POST("api/v1/forex/chart_submit/")
    fun submitChart(
        @Part("currency_pair") currencyPair: RequestBody,
        @Part("trading_strategy") tradingStrategy: RequestBody,
        @Part("time_frame") timeFrame: RequestBody,
        @Part chart_image: MultipartBody.Part
    ): Call<LLMForexChartResponse>

    @POST("api/v1/forex/submit/broker-information/")
    fun verifyBroker(@Body payload: BrokerageInformation): Call<ResponsePayload>
}
