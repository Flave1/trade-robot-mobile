package com.aid.trader.data.payload

data class ResponsePayload(
    val status_code: Int,
    val message: String,
    val data: Any
)

data class LLMForexChartResponse(
    val should_place: Boolean,
    val lot_size: String,
    val stop_loss_level: String,
    val take_profit_level: String,
    val close_trade: String,
    val should_buy: String,
    val should_sell: String,
    val pointers: String,
)