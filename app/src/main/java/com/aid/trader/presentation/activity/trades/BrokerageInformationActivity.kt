package com.aid.trader.presentation.activity.trades

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.aid.trader.R
import com.aid.trader.data.firebase.BrokerageInformation
import com.aid.trader.presentation.viewModel.BrokerageViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class BrokerageInformationActivity : ComponentActivity() {

    private val viewModel: BrokerageViewModel by viewModels()
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brokerage_information)
        auth = Firebase.auth
        dbRef = FirebaseDatabase.getInstance().reference
        val brokerServerNameInput = findViewById<EditText>(R.id.brokerServerNameInput)
        val accountLoginInput = findViewById<EditText>(R.id.accountLoginInput)
        val brokerPasswordInput = findViewById<EditText>(R.id.brokerPasswordInput)
        val saveButton = findViewById<Button>(R.id.saveBrokerageInfoButton)

        saveButton.setOnClickListener {
            val brokerServerName = brokerServerNameInput.text.toString().trim()
            val accountLogin = accountLoginInput.text.toString().trim()
            val brokerPassword = brokerPasswordInput.text.toString().trim()

            if (brokerServerName.isNotEmpty() && accountLogin.isNotEmpty() && brokerPassword.isNotEmpty()) {
                val brokerage = BrokerageInformation(
                    brokerServerName = brokerServerName,
                    accountLogin = accountLogin,
                    brokerPassword = brokerPassword
                )
                saveBrokerageInformation(brokerage)
            } else {
                // Show a message if any field is empty
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBrokerageInformation(broker: BrokerageInformation){
        dbRef.child("brokerage").child(auth.currentUser?.uid!!).push().setValue(broker)
            .addOnSuccessListener {
                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                val intent = Intent(this, TradeActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Error saving data", exception)
                Toast.makeText(this, "Failed to save brokerage information. Please try again.", Toast.LENGTH_LONG).show()
            }
    }
}
