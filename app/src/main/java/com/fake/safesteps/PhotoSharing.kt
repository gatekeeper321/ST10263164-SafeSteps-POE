package com.fake.safesteps

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fake.safesteps.databinding.ActivityMainBinding
import com.fake.safesteps.databinding.ActivityPhotoSharingBinding
import com.google.firebase.auth.FirebaseAuth

class PhotoSharing : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoSharingBinding
    private lateinit var aut : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPhotoSharingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        aut = FirebaseAuth.getInstance()
        binding.textView.text = "Welcome ${aut.currentUser!!.email}"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.option,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.SignOut){
            aut.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}