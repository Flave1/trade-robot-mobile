package com.aid.trader.interfaces

import com.aid.trader.data.firebase.BrokerageInformation
import com.aid.trader.data.payload.ResponsePayload
import com.aid.trader.helper.NetworkCallResult

interface BrokerRepositoryInterface {
    fun verifyBroker(payload: BrokerageInformation, callback: (NetworkCallResult<ResponsePayload>) -> Unit)
    fun addBrokerageInformation(brokerage: BrokerageInformation)
    fun getBrokerageInformation(documentId: String)
    fun updateBrokerageInformation(documentId: String, brokerage: BrokerageInformation)
    fun deleteBrokerageInformation(documentId: String)
    fun getBrokerageList(callback: (List<BrokerageInformation>) -> Unit)
}
