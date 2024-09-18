package com.aid.trader.helper

sealed class NetworkCallResult<out T> {
    data class Success<out T>(val data: T) : NetworkCallResult<T>()
    data class Error(val exception: Exception) : NetworkCallResult<Nothing>()
}
