package com.example.recipeapplication

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.recipeapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.addAuthStateListener {
            if (it.currentUser != null){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.tvRegister.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnLogin.setOnClickListener {
            var etEmail = binding.etEmail
            var etPassword = binding.etPassword
            var email = etEmail.text.toString()
            var password = etPassword.text.toString()
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("Logging in....").setCancelable(false)
            val alert = dialog.create()
            alert.show()

            if (email.isEmpty()) {
                alert.dismiss()
                etEmail.setError("This field cannot be left empty!")
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                alert.dismiss()
                etPassword.setError("This field cannot be left empty!")
                etPassword.requestFocus()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    alert.dismiss()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    alert.dismiss()
                    Log.d("error", it.exception.toString())
                    etPassword.text?.clear()
                    etEmail.setError("Invalid Email or Password!")
                    etPassword.setError("Invalid Email or Password!")
                    etEmail.requestFocus()
                }
            }
        }
    }
}