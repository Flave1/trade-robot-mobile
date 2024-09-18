package com.aid.trader.presentation.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aid.trader.R
import com.aid.trader.data.firebase.BrokerageInformation
import com.aid.trader.presentation.activity.conversational.ConversationsActivity

class BrokerAdapter(private val brokerList: List<BrokerageInformation>)
    : RecyclerView.Adapter<BrokerAdapter.BrokerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrokerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_broker, parent, false)
        return BrokerViewHolder(view)
    }

    override fun getItemCount(): Int = brokerList.size

    override fun onBindViewHolder(holder: BrokerViewHolder, position: Int) {
        val brokerage = brokerList[position]
        holder.bind(brokerage)
    }

    inner class BrokerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(brokerage: BrokerageInformation) {
            itemView.findViewById<TextView>(R.id.brokerServerName).text = brokerage.brokerServerName
            itemView.findViewById<TextView>(R.id.accountLogin).text = brokerage.accountLogin
            itemView.findViewById<TextView>(R.id.brokerPassword).text = brokerage.brokerPassword

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ConversationsActivity::class.java)
                intent.putExtra("brokerServerName", brokerage.brokerServerName)
                intent.putExtra("accountLogin", brokerage.accountLogin)
                intent.putExtra("brokerPassword", brokerage.brokerPassword)
                context.startActivity(intent)
            }
        }
    }
}


