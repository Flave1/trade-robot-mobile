package com.aid.trader.presentation.activity.conversational

import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.aid.trader.R
import com.aid.trader.databinding.ActivityConversationsBinding
import com.aid.trader.presentation.fragments.ChannelNameBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase

class ConversationsActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityConversationsBinding
    private lateinit var auth: FirebaseAuth
    private  lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        binding = ActivityConversationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setBackgroundResource(R.drawable.background)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_conversations)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener {
            val bottomSheet = ChannelNameBottomSheet()
            bottomSheet.show(supportFragmentManager, "ChannelNameBottomSheet")
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.channelsFragment -> {
                    supportActionBar?.show()
                    binding.fab.visibility= VISIBLE
//                    navController.popBackStack(R.id.conversationsFragment, true)
                }
                R.id.conversationsFragment -> {
                    supportActionBar?.hide()
                    binding.fab.visibility=GONE
                }
                else -> {
                    Log.d("Navigation", "Navigated to some other fragment")
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_conversations)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}