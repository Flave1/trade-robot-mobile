package com.aid.trader.data.firebase

data class Channels(
    val channelId: String? = null,
    val name: String?= null,
    val timestamp: String?= null
)

data class ChatMessage(
    val channelId: String? = null,
    val senderId: String? = null,
    var name: String? = null,
    val message: String? = null,
    val timestamp: String? = null,
    var read: Int = 0
)

data class ChannelWithLastMessage(
    val channelId: String,
    val name: String?= null,
    val lastMessage: String?=null,
    val timestamp: String?= null
)