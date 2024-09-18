package com.aid.trader.presentation.activity.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.aid.trader.MainActivity
import com.aid.trader.R
import com.aid.trader.data.firebase.UserInformation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var googleSignInButton: Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private  lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        auth = Firebase.auth

        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        submitButton = findViewById(R.id.submitButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_web_Id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInButton.setOnClickListener {
//            val signInClient = Identity.getSignInClient(this)
//            val signInRequest = BeginSignInRequest.builder()
//                .setGoogleIdTokenRequestOptions(
//                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                        .setSupported(true)
//                        .setServerClientId(getString(R.string.client_web_Id))
//                        .setFilterByAuthorizedAccounts(false)
//                        .build())
//                .build()
//
//            signInClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
//                    try {
//                        startIntentSenderForResult(result.pendingIntent.intentSender, RC_SIGN_IN,
//                            null, 0, 0, 0)
//                    } catch (e: IntentSender.SendIntentException) {
//                        Log.e("SignInActivity", "Google Sign-In failed", e)
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Log.e("SignInActivity", "Google Sign-In failed", e)
//                }
            signInWithClient()
        }

        submitButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            signInUser(email=email, password=password)
        }

    }

    private fun signInWithClient(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
        else{

        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            val account: GoogleSignInAccount? = task.result
            if(account != null){
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                signInUser(credential)
                Log.e("account", account.email.toString())
            }
        }else{
            Log.e("account", "SignIn failed")
        }
    }

    private fun signInUser(email: String, password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(baseContext, "User signed in successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
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
    private fun addUserToDatabase(name: String, uid: String){
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("userInformation").child(uid).setValue(UserInformation(name))
    }

    private fun signInUser(credential: AuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(baseContext, "Success", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Successfully signed in
                Log.d("GoogleSignIn", "signInWithGoogle:success, account: ${account?.email}")
                // Proceed with authenticated actions
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "signInWithGoogle:failed code=${e.statusCode}", e)
                // Handle the error, display a message to the user, etc.
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
