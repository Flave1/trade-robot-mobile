package com.aid.trader.data.payload

data class RequestPayload(
    val user_id: String,
    val query: String,
    val query_embedding: List<Float> = emptyList()
)

data class ChartRequestPayload(
    val user_id: String,
    val query: String,
    val query_embedding: List<Float> = emptyList()
)

