package com.aid.trader.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.aid.trader.R
import com.aid.trader.data.firebase.Channels
import com.aid.trader.data.firebase.ChatMessage
import com.aid.trader.data.firebase.UserInformation
import com.aid.trader.helper.Uten
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ChannelNameBottomSheet : BottomSheetDialogFragment() {

    private lateinit var etChannelName: EditText

    private lateinit var auth: FirebaseAuth
    private  lateinit var dbRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_new_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        etChannelName = view.findViewById(R.id.etChannelName)
        val btnSubmit: View = view.findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val channelName = etChannelName.text.toString().trim()
            if (channelName.isEmpty()) {
                etChannelName.error = "Channel name is required"
            } else {
//                Toast.makeText(requireContext(), "Channel Name: $channelName", Toast.LENGTH_SHORT).show()
                openNewConversation(etChannelName.text.toString().trim(), view)
            }
        }
    }

    private fun openNewConversation(channelName: String, view: View){

        CoroutineScope(Dispatchers.IO).launch {
            val userId = auth.currentUser?.uid
            try {
                val userInfo = getUserInformation(userId!!)
                val message = "Hello ${userInfo?.name} how can I help you today"
                if (userInfo != null) {
                    val channelId: UUID = UUID.randomUUID()
                    dbRef = FirebaseDatabase.getInstance().reference
                    dbRef.child("channels").child(userId).child(channelId.toString())
                        .setValue(Channels(channelId.toString(), channelName, Uten.getCurrentTimestamp()))
                        .addOnSuccessListener {
                            sendMessage(userId, channelId.toString(), message)
                            Toast.makeText(requireContext(), "Channel Name: $channelName created", Toast.LENGTH_LONG).show()
                            // Snackbar.make(view, "Channel Name Added", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                            dismiss()
                        }
                } else {
                    Log.d("Firebase", "User not found")
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error retrieving user information", e)
            }
        }

    }



    private suspend fun getUserInformation(userId: String): UserInformation? {
        return suspendCancellableCoroutine { continuation ->
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("userInformation")
            val userRef = usersRef.child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(UserInformation::class.java)
                    if (user != null) {
                        continuation.resume(user)
                    } else {
                        continuation.resume(null)  // or throw an exception
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    continuation.resumeWithException(databaseError.toException())
                }
            })

            continuation.invokeOnCancellation {
                // If coroutine is cancelled, handle cleanup if necessary
            }
        }
    }

    private fun sendMessage(senderUid: String?,  channelId: String?, message: String?){
        val senderRoom = channelId + senderUid
        val receiverRoom = senderUid + channelId
        var chatMessage = ChatMessage(channelId, channelId, "robot", message, Uten.getCurrentTimestamp())
        dbRef.child("conversations").child(senderRoom!!).child("messages").push().setValue(chatMessage)
            .addOnSuccessListener {
                chatMessage.read = 1
                dbRef.child("conversations").child(receiverRoom!!).child("messages").push().setValue(chatMessage)
            }
    }
}
