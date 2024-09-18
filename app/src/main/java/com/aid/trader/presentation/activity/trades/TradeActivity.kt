package com.aid.trader.presentation.activity.trades


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aid.trader.R
import com.aid.trader.data.firebase.BrokerageInformation
import com.aid.trader.presentation.adapters.BrokerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class TradeActivity : ComponentActivity() {

    private lateinit var brokerRecyclerView: RecyclerView
    private lateinit var brokerAdapter: BrokerAdapter
    private  lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var brokerList : ArrayList<BrokerageInformation>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade)
        auth = Firebase.auth
        dbRef = FirebaseDatabase.getInstance().reference
        brokerRecyclerView = findViewById(R.id.brokerRecyclerView)
        brokerRecyclerView.layoutManager = LinearLayoutManager(this)
        brokerList = ArrayList()
        getUserBrokers()
        brokerAdapter = BrokerAdapter(brokerList)
        brokerRecyclerView.adapter = brokerAdapter
        brokerAdapter.notifyDataSetChanged()
        val brokerButton = findViewById<Button>(R.id.brokerButton)
        brokerButton.setOnClickListener{
            gotoBrokerInformationForm()
        }
    }

    private fun gotoBrokerInformationForm(){
        val intent = Intent(this, BrokerageInformationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserBrokers(){
        dbRef.child("brokerage").child(auth.currentUser?.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    brokerList.clear()
                    for (postSnapshot in snapshot.children){
                        val broker = postSnapshot.getValue(BrokerageInformation::class.java)
                        if (broker != null) {
                            brokerList.add(broker)
                        }
                    }
                    Log.d("TradeActivity", "Number of brokers retrieved: ${brokerList.size}")
                    brokerAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TradeActivity", "Error retrieving brokers", error.toException())
                }
            })
    }


}
