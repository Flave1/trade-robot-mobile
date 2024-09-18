package com.aid.trader.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aid.trader.data.firebase.BrokerageInformation
import com.aid.trader.data.payload.ResponsePayload
import com.aid.trader.helper.NetworkCallResult
import com.aid.trader.interfaces.BrokerRepositoryInterface
import com.aid.trader.service.ApiService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BrokerageRepositoryImpl(private val apiService: ApiService): BrokerRepositoryInterface {

    private val fireStore = FirebaseFirestore.getInstance()
    private val brokerageCollection = fireStore.collection("brokerage_information")

    private val _response = MutableLiveData<NetworkCallResult<ResponsePayload>>()
    val response: LiveData<NetworkCallResult<ResponsePayload>> get() = _response

    override fun addBrokerageInformation(brokerage: BrokerageInformation) {
        brokerageCollection
            .add(brokerage)
            .addOnSuccessListener { documentReference ->
                val responsePayload = ResponsePayload(
                    status_code= 200,
                    data = documentReference.id,
                    message = "Brokerage information added successfully."
                )
                _response.value = NetworkCallResult.Success(responsePayload)
            }
            .addOnFailureListener { e ->
                _response.value = NetworkCallResult.Error(e)
            }
    }

    override fun verifyBroker(payload: BrokerageInformation, callback: (NetworkCallResult<ResponsePayload>) -> Unit) {
        apiService.verifyBroker(payload).enqueue(object : Callback<ResponsePayload> {
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

    override fun getBrokerageInformation(documentId: String) {
        brokerageCollection
            .document(documentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                documentSnapshot.toObject<BrokerageInformation>()?.let { brokerage ->
                    val responsePayload = ResponsePayload(
                        status_code= 200,
                        data = brokerage,
                        message = "Brokerage information retrieved."
                    )
                    _response.value = NetworkCallResult.Success(responsePayload)
                }
            }
            .addOnFailureListener { e ->
                _response.value = NetworkCallResult.Error(e)
            }
    }

    override fun updateBrokerageInformation(documentId: String, brokerage: BrokerageInformation) {
        brokerageCollection
            .document(documentId)
            .set(brokerage)
            .addOnSuccessListener {
                val responsePayload = ResponsePayload(
                    status_code= 200,
                    data = documentId,
                    message = "Brokerage information updated successfully."
                )
                _response.value = NetworkCallResult.Success(responsePayload)
            }
            .addOnFailureListener { e ->
                _response.value = NetworkCallResult.Error(e)
            }
    }

    override fun deleteBrokerageInformation(documentId: String) {
        brokerageCollection
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                val responsePayload = ResponsePayload(
                    status_code= 200,
                    data = documentId,
                    message = "Brokerage information deleted successfully."
                )
                _response.value = NetworkCallResult.Success(responsePayload)
            }
            .addOnFailureListener { e ->
                _response.value = NetworkCallResult.Error(e)
            }
    }

    override fun getBrokerageList(callback: (List<BrokerageInformation>) -> Unit) {
        brokerageCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val brokerages = mutableListOf<BrokerageInformation>()
                for (document in querySnapshot.documents) {
                    val brokerage = document.toObject<BrokerageInformation>()
                    brokerage?.let { brokerages.add(it) }
                }
                callback(brokerages)
            }
            .addOnFailureListener { e ->
                _response.value = NetworkCallResult.Error(e)
            }
    }

}
