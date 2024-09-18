package com.aid.trader.presentation.activity.conversational

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aid.trader.data.firebase.ChatMessage
import com.aid.trader.databinding.FragmentChannelConversationsBinding
import com.aid.trader.helper.Uten
import com.aid.trader.presentation.adapters.ChannelConversationsAdapter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ChannelConversationsFragment : Fragment() {

    private var _binding: FragmentChannelConversationsBinding? = null

    private val binding get() = _binding!!
    private lateinit var messageList: ArrayList<ChatMessage>
    private lateinit var conversationsAdapter: ChannelConversationsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var queryBox: EditText
    private lateinit var sendBtn: ImageView
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChannelConversationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageList = ArrayList()
        recyclerView = binding.channelMessagesRecyclerView
        dbRef = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth
        conversationsAdapter = ChannelConversationsAdapter(requireContext(), messageList)
        queryBox = binding.etMessage
        sendBtn = binding.imgSend

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = conversationsAdapter

        val channelName = arguments?.getString("channelName")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val receiverUid = arguments?.getString("channelId")

        binding.tvChannelName.text = channelName

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        observeMessages()

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.imgSend.setOnClickListener {
            sendMessage(senderUid, auth.currentUser?.uid, receiverUid)
        }
    }

    private fun observeMessages(){
        dbRef.child("conversations").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children){
                        try {
                            val message = postSnapshot.getValue(ChatMessage::class.java)
                            messageList.add(message!!)
                        }catch (ex: Exception){
                            continue
                        }
                    }
                    conversationsAdapter.notifyDataSetChanged()
                    recyclerView.post {
                        recyclerView.scrollToPosition(conversationsAdapter.itemCount - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage(senderUid: String?, name: String?, channelId: String?){
        val message = queryBox.text.toString()
        var chatMessage = ChatMessage(channelId, senderUid, name, message,
            Uten.getCurrentTimestamp())
        dbRef.child("conversations").child(senderRoom!!).child("messages").push().setValue(chatMessage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatMessage.read = 1
                    chatMessage.name = "robot"
                    dbRef.child("conversations").child(receiverRoom!!).child("messages").push().setValue(chatMessage)
                        .addOnCompleteListener { receiverTask ->
                            if (receiverTask.isSuccessful) {
                                Log.e("Firebase", "Success on add message to receiver's room", receiverTask.exception)
                            } else {
                                // Handle failure to add message to receiver's room
                                Log.e("Firebase", "Failed to add message to receiver's room", receiverTask.exception)
                                // You can show a failure message to the user or retry the operation here
                            }
                        }
                        .addOnFailureListener { e ->
                            // Handle failure to add message to sender's room
                            Log.e("Firebase", "Failed to add message to sender's room", e)
                            // You can show a failure message to the user or retry the operation here
                        }
                } else {
                    // Handle failure to add message to sender's room
                    Log.e("Firebase", "Failed to add message to sender's room", task.exception)
                    // You can show a failure message to the user or retry the operation here
                }
            }

        binding.etMessage.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}