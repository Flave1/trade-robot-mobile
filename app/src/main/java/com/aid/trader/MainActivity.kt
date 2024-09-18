package com.aid.trader

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.aid.trader.data.firebase.UserInformation
import com.aid.trader.data.payload.RequestPayload
import com.aid.trader.helper.Constants
import com.aid.trader.helper.Constants.REQUEST_CODE_SYSTEM_ALERT_WINDOW
import com.aid.trader.helper.Constants.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
import com.aid.trader.helper.NetworkCallResult
import com.aid.trader.presentation.activity.authentication.SignInActivity
import com.aid.trader.presentation.activity.trades.TradeActivity
import com.aid.trader.presentation.viewModel.MainViewModel
import com.aid.trader.presentation.viewModel.OverlayViewModel
import com.aid.trader.service.ForegroundService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private val viewModel: MainViewModel by viewModels()
    private val overlayViewModel: OverlayViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private  lateinit var dbRef: DatabaseReference
    private lateinit var userId: TextView
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        init()
    }

    private fun init() {
        auth = Firebase.auth

//        signInUser("favouremmanuel4333@gmail.com", "852365")
        userId = findViewById(R.id.userId)
        if(auth != null && auth?.currentUser != null)
            userId.text = auth?.currentUser!!.uid
//        checkPermissions()
//

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_fno -> {
//                    signUpUser("favouremmanuel433@gmail.com", "852365")
//                    signInUser("favouremmanuel4333@gmail.com", "852365")
                    signOutUser()
                    true
                }
                R.id.navigation_portfolio -> {
                    signInUser("favouremmanuel433@gmail.com", "852365")
                    true
                }
                else -> false
            }
        }
        observeDataChange()
        animateAction()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val placeTrade: Button = findViewById(R.id.place_trade)
        placeTrade.setOnClickListener{
            gotoStartTrade()
        }

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // User is signed out
                Toast.makeText(baseContext, "User has been signed out.", Toast.LENGTH_SHORT).show()
                // Optionally, redirect the user to the sign-in screen or another activity
                startActivity(Intent(this, SignInActivity::class.java))
                finish() // End current activity
            } else {
                // User is signed in
                Toast.makeText(baseContext, "User is signed in: ${user.email}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    public override fun onStart() {
        super.onStart()
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            Toast.makeText(this, "User account not signed", Toast.LENGTH_LONG)
//        }

        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun signUpUser(email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    addUserToDatabase("Flave", user?.uid.toString())
                    Toast.makeText(baseContext,   "Registration success. ${user?.uid}", Toast.LENGTH_LONG ).show()
                } else {
                    Log.d("registration", task.toString())
                    Toast.makeText(baseContext,   "Registration failed. ", Toast.LENGTH_LONG ).show()

                }
            }
    }

    private fun signInUser(email: String, password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(baseContext, "Success", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOutUser() {
        auth.signOut()
        Toast.makeText(baseContext, "Signed out successfully.", Toast.LENGTH_SHORT).show()
    }


    private fun getUserInformation(){
        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = it.displayName
            val email = it.email
            val photoUrl = it.photoUrl

            // Check if user's email is verified
            val emailVerified = it.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            val uid = it.uid
        }
    }

    private fun addUserToDatabase(name: String, uid: String){
            dbRef = FirebaseDatabase.getInstance().reference
            dbRef.child("userInformation").child(uid).setValue(UserInformation(name))
    }
    private fun observeUserCreation(){
        dbRef.child("userInformation").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(UserInformation::class.java)
//                    add user to current user list
//                    and notify the adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkPermissions(){

        val btnRequestMediaProjection: Button = findViewById(R.id.btnRequestMediaProjection)
        btnRequestMediaProjection.setOnClickListener {
            requestMediaProjectionPermission()
        }
        if (checkWriteExternalStoragePermission()) {
            Log.d("Permission", "WRITE_EXTERNAL_STORAGE permission granted")
        } else {
            requestWriteExternalStoragePermission()
        }

        checkAndRequestOverlayPermission()

    }

    private fun checkAndRequestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, REQUEST_CODE_SYSTEM_ALERT_WINDOW)
            }
        }
    }

    private fun requestMediaProjectionPermission() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, REQUEST_CODE_SCREENSHOT)
    }

    private fun checkWriteExternalStoragePermission(): Boolean {
        val writeExternalStoragePermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "WRITE_EXTERNAL_STORAGE permission granted")
                } else {
                    Log.d("Permission", "WRITE_EXTERNAL_STORAGE permission denied")
                }
            }

            REQUEST_CODE_SYSTEM_ALERT_WINDOW -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        Log.d("Permission", "WRITE_EXTERNAL_STORAGE permission granted")
                    } else {
                        Log.d("Permission", "WRITE_EXTERNAL_STORAGE permission denied")
                    }
                }
            }
        }
    }

    private fun startOverlayService(resultCode: Int, data: Intent) {
        val intent = Intent(this, ForegroundService::class.java).apply {
            putExtra("resultCode", resultCode)
            putExtra("data", data)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCREENSHOT && resultCode == Activity.RESULT_OK && data != null) {
            startOverlayService(resultCode, data)
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun apiCall(){
        val payload = RequestPayload(
            user_id = "123",
            query = "what model are you"
        )
        viewModel.updateForex(payload)
    }

    private fun apiCall2(){
        val chart = intent?.getStringExtra("chart") as File
        overlayViewModel.submitChart(chart, "GBP/USD", "24hrs", "Any Strategy")
    }

    private fun observeDataChange(){
        viewModel.response.observe(this, Observer { result ->
            when (result) {
                is NetworkCallResult.Success -> {
                    val response = result.data
                    Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                }
                is NetworkCallResult.Error -> {
                    Log.d("NetworkCallResult.Error", result.exception.toString())
                    Toast.makeText(this, "Error: ${result.exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        })

        overlayViewModel.response.observe(this, Observer { result ->
            when (result) {
                is NetworkCallResult.Success -> {
                    val response = result.data
                    Toast.makeText(this, response.pointers, Toast.LENGTH_LONG).show()
                }
                is NetworkCallResult.Error -> {
                    Log.d("NetworkCallResult.Error", result.exception.toString())
                    Toast.makeText(this, "Error: ${result.exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun animateAction(){

        var actionView: View = findViewById(R.id.actionView)!!
        val animator = ValueAnimator.ofFloat(0.3f, 1f)
        animator.duration = 1000 // 1 second
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            actionView.alpha = animatedValue
        }
        animator.start()
    }

    private fun gotoStartTrade(){
        val intent = Intent(this, TradeActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CODE_SCREENSHOT = 1001
    }

    //    override fun onDestroy() {
//        super.onDestroy()
//    }

    //    private fun toggleOverlay() {
//        if (isOverlayActive) {
//            overlayView?.removeOverlay()
//            isOverlayActive = false
//        } else {
//            overlayView = FullScreenNotificationView(this@MainActivity)
//            overlayView?.open()
//            isOverlayActive = true
//        }
//    }
}
