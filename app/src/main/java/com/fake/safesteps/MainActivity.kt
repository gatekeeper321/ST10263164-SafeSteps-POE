package com.fake.safesteps

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fake.safesteps.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, PhotoSharing::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val currentUser = auth.currentUser!!.email.toString()
                    Toast.makeText(this,"Welcome : ${currentUser}", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, PhotoSharing::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val intent = Intent(this, PhotoSharing::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }
}