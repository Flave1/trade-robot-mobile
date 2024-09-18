package com.aid.trader.helper

enum class ConversationEnums(val value: Int) {
    STARTED(1),
    ABORTED(2),
    SELL(3),
    BUY(3),
    HOLD(3),
    CLOSE(3);

    companion object {
        fun fromValue(value: Int): ConversationEnums? {
            return values().find { it.value == value }
        }
    }
}