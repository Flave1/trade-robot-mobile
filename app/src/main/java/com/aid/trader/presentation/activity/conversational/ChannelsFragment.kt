package com.aid.trader.presentation.activity.conversational

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aid.trader.R
import com.aid.trader.data.firebase.ChannelWithLastMessage
import com.aid.trader.data.firebase.Channels
import com.aid.trader.data.firebase.ChatMessage
import com.aid.trader.databinding.FragmentChannelsBinding
import com.aid.trader.presentation.adapters.ChannelAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var channelAdapter: ChannelAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var channelList: ArrayList<ChannelWithLastMessage>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        recyclerView = binding.recyclerViewChats
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        channelList = ArrayList()
        getChannels()
        channelAdapter = ChannelAdapter(channelList) { chat ->
            val bundle = Bundle().apply {
                putString("channelName", chat.name)
                putString("channelId", chat.channelId)
            }
            findNavController().navigate(R.id.action_channelsFragment_to_conversationsFragment, bundle)
        }
        recyclerView.adapter = channelAdapter

    }

    private fun getChannels() {
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("channels").child(auth.currentUser?.uid.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val tempChannelList = mutableListOf<ChannelWithLastMessage>()

                for (postSnapshot in snapshot.children) {
                    val channel = postSnapshot.getValue(Channels::class.java)
                    val room = auth.currentUser?.uid+channel?.channelId
                    channel?.let {
                        dbRef.child("conversations").child(room).child("messages")
                            .orderByChild("timestamp")
                            .limitToLast(1)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(messageSnapshot: DataSnapshot) {
                                    for (msgSnapshot in messageSnapshot.children) {
                                        val lastMessage = msgSnapshot.getValue(ChatMessage::class.java)
                                        if (lastMessage != null) {
                                            tempChannelList.add(ChannelWithLastMessage(channel.channelId.toString(), channel.name, lastMessage.message, lastMessage.timestamp))
                                        }
                                    }

                                    // Sort by last message timestamp
                                    tempChannelList.sortByDescending { it.timestamp }

                                    // Update the UI with the sorted list
                                    channelList.clear()
                                    channelList.addAll(tempChannelList.map { it })
                                    channelAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle database error
                                }
                            })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }


//    private fun getChannels(){
//        dbRef = FirebaseDatabase.getInstance().reference
//        dbRef.child("channels").child(auth.currentUser?.uid.toString()).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                channelList.clear()
//                for (postSnapshot in snapshot.children){
//                    val channel = postSnapshot.getValue(Channels::class.java)
//                    channelList.add(channel!!)
//                }
//                channelList.sortedByDescending { it.timestamp }
//                channelAdapter.notifyDataSetChanged()
//            }
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}