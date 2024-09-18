package com.aid.trader.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aid.trader.R
import com.aid.trader.data.firebase.ChannelWithLastMessage
import com.aid.trader.helper.Uten

class ChannelAdapter(
    private val channelsList: List<ChannelWithLastMessage>,
    private val onItemClick: (ChannelWithLastMessage) -> Unit) : RecyclerView.Adapter<ChannelAdapter.ChatViewHolder>() {

    private val currentDate = Uten.getCurrentDate()
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = channelsList[position]
        holder.imgProfile.setImageResource(R.drawable.round_tag_faces_24)
        holder.tvName.text = chat.name
        holder.tvLastMessage.text = chat.lastMessage
        holder.tvTime.text = formatDate(chat.timestamp)

        holder.itemView.setOnClickListener {
            onItemClick(chat)
        }
    }

    override fun getItemCount(): Int = channelsList.size

    private fun formatDate(timestamp: String?): String {
        var dtslpit = timestamp!!.split(" ")
        return if(currentDate != dtslpit[0])
            timestamp
        else
            dtslpit[1].substring(0, 5)
    }
    }