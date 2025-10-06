package com.fake.safesteps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fake.safesteps.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        auth = FirebaseAuth.getInstance()

        binding.btnSignIn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this,exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

        binding.btnSkip.setOnClickListener {
            val intent = Intent(this, AlertActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}