package com.example.recipeapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        binding.tvRegister.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnRegister.setOnClickListener{
            val etEmail = binding.etEmail
            val etPassword = binding.etPassword
            val etConfirmPassword = binding.etConfirmPassword
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("Please wait...").setCancelable(false)
            val alert = dialog.create()
            alert.show()

            if(etEmail.text.toString().isEmpty()){
                alert.dismiss()
                etEmail.error = "This field cannot be left empty!"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if(etPassword.text.toString().isEmpty()){
                alert.dismiss()
                etPassword.error = "This field cannot be left empty!"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if(etConfirmPassword.text.toString().isEmpty()){
                alert.dismiss()
                etConfirmPassword.error = "This field cannot be left empty!"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            if(etPassword.text.toString().length < 8){
                alert.dismiss()
                etPassword.error = "Passwords must be at least 8 characters!"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if(etPassword.text.toString() != etConfirmPassword.text.toString()){
                alert.dismiss()
                etPassword.error = "Passwords does not match"
                etConfirmPassword.error = "Passwords does not match"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString()).addOnCompleteListener{ err ->
                if (err.isSuccessful){
                    val userId = mAuth.currentUser!!.uid
                    val userMap = hashMapOf(
                        "userID" to userId,
                        "email" to etEmail.text.toString(),
                        "password" to etPassword.text.toString()
                    )
                    firestore.collection("user").document().set(userMap).addOnSuccessListener {
                        Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                        alert.dismiss()
                        mAuth.signInWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString()).addOnCompleteListener {
                            if (it.isSuccessful) {
                                alert.dismiss()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                alert.dismiss()
                                Log.d("error", it.exception.toString())
                            }
                        }
                    }.addOnFailureListener { error ->
                        alert.dismiss()
                        Toast.makeText(this, error.message.toString(), Toast.LENGTH_SHORT).show()
                        Log.d("error", error.message.toString())
                    }
                }else{
                    alert.dismiss()
                    Toast.makeText(this, err.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}