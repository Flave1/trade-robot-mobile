package com.aid.trader.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aid.trader.R
import com.aid.trader.data.firebase.ChatMessage
import com.aid.trader.helper.Uten
import com.google.firebase.auth.FirebaseAuth

class ChannelConversationsAdapter(private val context: Context, private val messageList: ArrayList<ChatMessage>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val received = 1
    private val sent = 2
    private val currentDate = Uten.getCurrentDate()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 1){
            val view: View = LayoutInflater.from(context).inflate(R.layout.section_received_message, parent, false)
            ReceiveMessagesViewHolder(view)
        }else{
            val view: View = LayoutInflater.from(context).inflate(R.layout.section_sent_message, parent, false)
            SentMessagesViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder.javaClass == SentMessagesViewHolder::class.java) {
            var viewHolder = holder as SentMessagesViewHolder
            holder.message.text = currentMessage.message
            holder.time.text = formatDate(currentMessage.timestamp)
            if (currentMessage.read == 0)
                holder.imgRead.visibility = View.GONE
            else
                holder.imgRead.visibility = View.VISIBLE
        } else {
            var viewHolder = holder as ReceiveMessagesViewHolder
            holder.message.text = currentMessage.message
            holder.time.text = formatDate(currentMessage.timestamp)
        }
    }


    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            sent
        }else{
            received
        }
    }

    private fun formatDate(timestamp: String?): String {
        var dtslpit = timestamp!!.split(" ")
        return if(currentDate != dtslpit[0])
            timestamp
        else
            dtslpit[1].substring(0, 5)
    }

    class SentMessagesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val message: TextView = itemView.findViewById(R.id.tvMessage)
        val time: TextView = itemView.findViewById(R.id.tvTime)
        val imgRead: ImageView = itemView.findViewById((R.id.imgRead))
    }
    class ReceiveMessagesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val message: TextView = itemView.findViewById(R.id.tvMessage)
        val time: TextView = itemView.findViewById(R.id.tvTime)
    }
}