package com.aid.trader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ChartBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val description = intent.getStringExtra("description")
        val currencyPairs = intent.getStringExtra("currency_pairs")
        val tradingStrategy = intent.getStringExtra("trading_strategy")
        val timeFrame = intent.getStringExtra("time_frame")

        Toast.makeText(context, "Received: $description, $currencyPairs, $tradingStrategy, $timeFrame", Toast.LENGTH_LONG).show()

        // You can also start an activity here if needed
        // val newIntent = Intent(context, AnotherActivity::class.java)
        // newIntent.putExtra("description", description)
        // newIntent.putExtra("currency_pairs", currencyPairs)
        // newIntent.putExtra("trading_strategy", tradingStrategy)
        // newIntent.putExtra("time_frame", timeFrame)
        // context.startActivity(newIntent)
    }
}