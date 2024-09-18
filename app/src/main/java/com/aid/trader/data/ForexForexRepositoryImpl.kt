package com.aid.trader.data

import com.aid.trader.data.payload.LLMForexChartResponse
import com.aid.trader.data.payload.RequestPayload
import com.aid.trader.data.payload.ResponsePayload
import com.aid.trader.helper.NetworkCallResult
import com.aid.trader.interfaces.ForexRepositoryInterface
import com.aid.trader.service.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ForexForexRepositoryImpl(private val apiService: ApiService) : ForexRepositoryInterface {

    override fun updateForex(payload: RequestPayload, callback: (NetworkCallResult<ResponsePayload>) -> Unit) {
        apiService.updateForex(payload).enqueue(object : Callback<ResponsePayload> {
            override fun onResponse(call: Call<ResponsePayload>, response: Response<ResponsePayload>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(NetworkCallResult.Success(it))
                    } ?: callback(NetworkCallResult.Error(Exception("Response body is null")))
                } else {
                    callback(NetworkCallResult.Error(Exception("Network call failed: ${response.code()}")))
                }
            }

            override fun onFailure(call: Call<ResponsePayload>, t: Throwable) {
                callback(NetworkCallResult.Error(Exception(t)))
            }
        })
    }

    override fun submitChart(
        imageFile: File,
        currencyPairs: String,
        timeFrame: String,
        tradingStrategy: String,
        callback: (NetworkCallResult<LLMForexChartResponse>) -> Unit
    ) {
        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imgBody = MultipartBody.Part.createFormData("chart_image", imageFile.name, requestFile)
        val currencyPairBody = currencyPairs.toRequestBody("text/plain".toMediaTypeOrNull())
        val tradingStrategyBody = tradingStrategy.toRequestBody("text/plain".toMediaTypeOrNull())
        val timeFrameBody = timeFrame.toRequestBody("text/plain".toMediaTypeOrNull())

        // Log the request details
        val request = apiService.submitChart(currencyPairBody, tradingStrategyBody, timeFrameBody, imgBody).request()

        apiService.submitChart(currencyPairBody, tradingStrategyBody, timeFrameBody, imgBody).enqueue(object : Callback<LLMForexChartResponse> {
            override fun onResponse(call: Call<LLMForexChartResponse>, response: Response<LLMForexChartResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(NetworkCallResult.Success(it))
                    } ?: callback(NetworkCallResult.Error(Exception("Response body is null")))
                } else {
                    callback(NetworkCallResult.Error(Exception("Network call failed: ${response.code()} - ${response.message()}")))
                }
            }

            override fun onFailure(call: Call<LLMForexChartResponse>, t: Throwable) {
                callback(NetworkCallResult.Error(Exception(t)))
            }
        })
    }

}
